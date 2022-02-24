package com.example.application.views.collaborativemasterdetailproduct;

import com.example.application.data.entity.SampleFoodProduct;
import com.example.application.data.service.SampleFoodProductService;
import com.example.application.views.MainLayout;
import com.vaadin.collaborationengine.CollaborationAvatarGroup;
import com.vaadin.collaborationengine.CollaborationBinder;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import elemental.json.Json;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.util.UriUtils;

@PageTitle("Collaborative Master Detail Product")
@Route(value = "collaborative-master-detail-product/:sampleFoodProductID?/:action?(edit)", layout = MainLayout.class)
@PermitAll
public class CollaborativeMasterDetailProductView extends Div implements BeforeEnterObserver {

    private final String SAMPLEFOODPRODUCT_ID = "sampleFoodProductID";
    private final String SAMPLEFOODPRODUCT_EDIT_ROUTE_TEMPLATE = "collaborative-master-detail-product/%s/edit";

    private Grid<SampleFoodProduct> grid = new Grid<>(SampleFoodProduct.class, false);

    CollaborationAvatarGroup avatarGroup;

    private Upload image;
    private Image imagePreview;
    private TextField name;
    private TextField eanCode;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private CollaborationBinder<SampleFoodProduct> binder;

    private SampleFoodProduct sampleFoodProduct;

    private SampleFoodProductService sampleFoodProductService;

    public CollaborativeMasterDetailProductView(@Autowired SampleFoodProductService sampleFoodProductService) {
        this.sampleFoodProductService = sampleFoodProductService;
        addClassNames("collaborative-master-detail-product-view", "flex", "flex-col", "h-full");

        // UserInfo is used by Collaboration Engine and is used to share details
        // of users to each other to able collaboration. Replace this with
        // information about the actual user that is logged, providing a user
        // identifier, and the user's real name. You can also provide the users
        // avatar by passing an url to the image as a third parameter, or by
        // configuring an `ImageProvider` to `avatarGroup`.
        UserInfo userInfo = new UserInfo(UUID.randomUUID().toString(), "Steve Lange");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        avatarGroup = new CollaborationAvatarGroup(userInfo, null);
        avatarGroup.getStyle().set("visibility", "hidden");

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        LitRenderer<SampleFoodProduct> imageRenderer = LitRenderer.<SampleFoodProduct>of(
                "<span style='border-radius: 50%; overflow: hidden; display: flex; align-items: center; justify-content: center; width: 64px; height: 64px'><img style='max-width: 100%' src=${item.image} /></span>")
                .withProperty("image", SampleFoodProduct::getImage);
        grid.addColumn(imageRenderer).setHeader("Image").setWidth("96px").setFlexGrow(0);

        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("eanCode").setAutoWidth(true);
        grid.setItems(query -> sampleFoodProductService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent()
                        .navigate(String.format(SAMPLEFOODPRODUCT_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(CollaborativeMasterDetailProductView.class);
            }
        });

        // Configure Form
        binder = new CollaborationBinder<>(SampleFoodProduct.class, userInfo);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        attachImageUpload(image, imagePreview);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.sampleFoodProduct == null) {
                    this.sampleFoodProduct = new SampleFoodProduct();
                }
                binder.writeBean(this.sampleFoodProduct);
                this.sampleFoodProduct.setImage(imagePreview.getSrc());

                sampleFoodProductService.update(this.sampleFoodProduct);
                clearForm();
                refreshGrid();
                Notification.show("SampleFoodProduct details stored.");
                UI.getCurrent().navigate(CollaborativeMasterDetailProductView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the sampleFoodProduct details.");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UUID> sampleFoodProductId = event.getRouteParameters().get(SAMPLEFOODPRODUCT_ID).map(UUID::fromString);
        if (sampleFoodProductId.isPresent()) {
            Optional<SampleFoodProduct> sampleFoodProductFromBackend = sampleFoodProductService
                    .get(sampleFoodProductId.get());
            if (sampleFoodProductFromBackend.isPresent()) {
                populateForm(sampleFoodProductFromBackend.get());
            } else {
                Notification.show(String.format("The requested sampleFoodProduct was not found, ID = %d",
                        sampleFoodProductId.get()), 3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(CollaborativeMasterDetailProductView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("flex flex-col");
        editorLayoutDiv.setWidth("400px");

        Div editorDiv = new Div();
        editorDiv.setClassName("p-l flex-grow");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        Label imageLabel = new Label("Image");
        imagePreview = new Image();
        imagePreview.setWidth("100%");
        image = new Upload();
        image.getStyle().set("box-sizing", "border-box");
        image.getElement().appendChild(imagePreview.getElement());
        name = new TextField("Name");
        eanCode = new TextField("Ean Code");
        Component[] fields = new Component[]{imageLabel, image, name, eanCode};

        for (Component field : fields) {
            ((HasStyle) field).addClassName("full-width");
        }
        formLayout.add(fields);
        editorDiv.add(avatarGroup, formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("w-full flex-wrap bg-contrast-5 py-s px-l");
        buttonLayout.setSpacing(true);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void attachImageUpload(Upload upload, Image preview) {
        ByteArrayOutputStream uploadBuffer = new ByteArrayOutputStream();
        upload.setAcceptedFileTypes("image/*");
        upload.setReceiver((fileName, mimeType) -> {
            return uploadBuffer;
        });
        upload.addSucceededListener(e -> {
            String mimeType = e.getMIMEType();
            String base64ImageData = Base64.getEncoder().encodeToString(uploadBuffer.toByteArray());
            String dataUrl = "data:" + mimeType + ";base64,"
                    + UriUtils.encodeQuery(base64ImageData, StandardCharsets.UTF_8);
            upload.getElement().setPropertyJson("files", Json.createArray());
            preview.setSrc(dataUrl);
            uploadBuffer.reset();
        });
        preview.setVisible(false);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(SampleFoodProduct value) {
        this.sampleFoodProduct = value;
        String topic = null;
        if (this.sampleFoodProduct != null && this.sampleFoodProduct.getId() != null) {
            topic = "sampleFoodProduct/" + this.sampleFoodProduct.getId();
            avatarGroup.getStyle().set("visibility", "visible");
        } else {
            avatarGroup.getStyle().set("visibility", "hidden");
        }
        binder.setTopic(topic, () -> this.sampleFoodProduct);
        avatarGroup.setTopic(topic);
        this.imagePreview.setVisible(value != null);
        if (value == null) {
            this.imagePreview.setSrc("");
        } else {
            this.imagePreview.setSrc(value.getImage());
        }

    }
}