package com.example.application.views;

import com.example.application.data.entity.User;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.addressform.AddressFormView;
import com.example.application.views.adminrole.AdminRoleView;
import com.example.application.views.cardlist.CardListView;
import com.example.application.views.chat.ChatView;
import com.example.application.views.checkoutform.CheckoutFormView;
import com.example.application.views.collaborativemasterdetail.CollaborativeMasterDetailView;
import com.example.application.views.collaborativemasterdetailaddress.CollaborativeMasterDetailAddressView;
import com.example.application.views.collaborativemasterdetailbook.CollaborativeMasterDetailBookView;
import com.example.application.views.collaborativemasterdetailproduct.CollaborativeMasterDetailProductView;
import com.example.application.views.creditcardform.CreditCardFormView;
import com.example.application.views.dashboard.DashboardView;
import com.example.application.views.editor.EditorView;
import com.example.application.views.empty.EmptyView;
import com.example.application.views.helloworld.HelloWorldView;
import com.example.application.views.list.ListView;
import com.example.application.views.map.MapView;
import com.example.application.views.masterdetail.MasterDetailView;
import com.example.application.views.masterdetailaddress.MasterDetailAddressView;
import com.example.application.views.masterdetailbook.MasterDetailBookView;
import com.example.application.views.masterdetailproduct.MasterDetailProductView;
import com.example.application.views.personform.PersonFormView;
import com.example.application.views.userrole.UserRoleView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    /**
     * A simple navigation item component, based on ListItem element.
     */
    public static class MenuItemInfo extends ListItem {

        private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, String iconClass, Class<? extends Component> view) {
            this.view = view;
            RouterLink link = new RouterLink();
            // Use Lumo classnames for various styling
            link.addClassNames("flex", "mx-s", "p-s", "relative", "text-secondary");
            link.setRoute(view);

            Span text = new Span(menuTitle);
            // Use Lumo classnames for various styling
            text.addClassNames("font-medium", "text-s");

            link.add(new LineAwesomeIcon(iconClass), text);
            add(link);
        }

        public Class<?> getView() {
            return view;
        }

        /**
         * Simple wrapper to create icons using LineAwesome iconset. See
         * https://icons8.com/line-awesome
         */
        @NpmPackage(value = "line-awesome", version = "1.3.0")
        public static class LineAwesomeIcon extends Span {
            public LineAwesomeIcon(String lineawesomeClassnames) {
                // Use Lumo classnames for suitable font size and margin
                addClassNames("me-s", "text-l");
                if (!lineawesomeClassnames.isEmpty()) {
                    addClassNames(lineawesomeClassnames);
                }
            }
        }

    }

    private H1 viewTitle;

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        addToDrawer(createDrawerContent());
    }

    private Component createHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassName("text-secondary");
        toggle.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames("m-0", "text-l");

        Header header = new Header(toggle, viewTitle);
        header.addClassNames("bg-base", "border-b", "border-contrast-10", "box-border", "flex", "h-xl", "items-center",
                "w-full");
        return header;
    }

    private Component createDrawerContent() {
        H2 appName = new H2("Vaadin 22 Java examples");
        appName.addClassNames("flex", "items-center", "h-xl", "m-0", "px-m", "text-m");

        com.vaadin.flow.component.html.Section section = new com.vaadin.flow.component.html.Section(appName,
                createNavigation(), createFooter());
        section.addClassNames("flex", "flex-col", "items-stretch", "max-h-full", "min-h-full");
        return section;
    }

    private Nav createNavigation() {
        Nav nav = new Nav();
        nav.addClassNames("border-b", "border-contrast-10", "flex-grow", "overflow-auto");
        nav.getElement().setAttribute("aria-labelledby", "views");

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        list.addClassNames("list-none", "m-0", "p-0");
        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems()) {
            if (accessChecker.hasAccess(menuItem.getView())) {
                list.add(menuItem);
            }

        }
        return nav;
    }

    private MenuItemInfo[] createMenuItems() {
        return new MenuItemInfo[]{ //
                new MenuItemInfo("Empty", "la la-file", EmptyView.class), //

                new MenuItemInfo("Hello World", "la la-globe", HelloWorldView.class), //

                new MenuItemInfo("Dashboard", "la la-chart-area", DashboardView.class), //

                new MenuItemInfo("Card List", "la la-list", CardListView.class), //

                new MenuItemInfo("List", "la la-th", ListView.class), //

                new MenuItemInfo("Master Detail", "la la-columns", MasterDetailView.class), //

                new MenuItemInfo("Master Detail Address", "la la-columns", MasterDetailAddressView.class), //

                new MenuItemInfo("Master Detail Book", "la la-columns", MasterDetailBookView.class), //

                new MenuItemInfo("Master Detail Product", "la la-columns", MasterDetailProductView.class), //

                new MenuItemInfo("Collaborative Master Detail", "la la-columns", CollaborativeMasterDetailView.class), //

                new MenuItemInfo("Collaborative Master Detail Address", "la la-columns",
                        CollaborativeMasterDetailAddressView.class), //

                new MenuItemInfo("Collaborative Master Detail Book", "la la-columns",
                        CollaborativeMasterDetailBookView.class), //

                new MenuItemInfo("Collaborative Master Detail Product", "la la-columns",
                        CollaborativeMasterDetailProductView.class), //

                new MenuItemInfo("Person Form", "la la-user", PersonFormView.class), //

                new MenuItemInfo("Address Form", "la la-map-marker", AddressFormView.class), //

                new MenuItemInfo("Credit Card Form", "la la-credit-card", CreditCardFormView.class), //

                new MenuItemInfo("Map", "la la-map", MapView.class), //

                new MenuItemInfo("Chat", "la la-comments", ChatView.class), //

                new MenuItemInfo("Editor", "la la-edit", EditorView.class), //

                new MenuItemInfo("Checkout Form", "la la-credit-card", CheckoutFormView.class), //

                new MenuItemInfo("User Role", "la la-globe", UserRoleView.class), //

                new MenuItemInfo("Admin Role", "la la-globe", AdminRoleView.class), //

        };
    }

    private Footer createFooter() {
        Footer layout = new Footer();
        layout.addClassNames("flex", "items-center", "my-s", "px-m", "py-xs");

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getName(), user.getProfilePictureUrl());
            avatar.addClassNames("me-xs");

            ContextMenu userMenu = new ContextMenu(avatar);
            userMenu.setOpenOnClick(true);
            userMenu.addItem("Logout", e -> {
                authenticatedUser.logout();
            });

            Span name = new Span(user.getName());
            name.addClassNames("font-medium", "text-s", "text-secondary");

            layout.add(avatar, name);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
