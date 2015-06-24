(ns glados.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [gravatar :refer [avatar-url]])
  (:import goog.History))


;; UI Elements -----------------------------------------------------------------
(defn navbar-item
  [title symbol url]
  [:li {:class (when (= symbol (session/get :page)) "active")}
   [:a {:href url} title]])

(defn navbar []
  [:div.navbar.navbar-inverse.navbar-fixed-top
   [:div.container
    [:div.navbar-header
     [:img {:src (avatar-url "merino.bailon@gmail.com" :size 50)}]]
    [:div.navbar-collapse.collapse
     [:ul.nav.navbar-nav
      (navbar-item "Home" :home "#/")
      (navbar-item "Projects" :projects "#/projects")
      (navbar-item "About" :about "#/about")]]]])

(defn about-page []
  [:div.container
   [:h3 "Know more about the machine"]
   [:p "...work in progress"]])

(defn home-page []
  [:div.container
   [:div.jumbotron
    [:h1 "Welcome to Glados"]
    [:p "...in which I will experiment with Clojure"]]
   [:div.row
    [:div.col-md-12
     [:h2 "Welcome to ClojureScript"]]]
   (when-let [docs (session/get :docs)]
     [:div.row
      [:div.col-md-12
       [:div {:dangerouslySetInnerHTML
              {:__html (md->html docs)}}]]])])

(defn projects-page []
  [:div.container
   [:h2 "Projects"]
   [:p "...work in progress"]])


;; Navigation ------------------------------------------------------------------
(def pages
  {:home #'home-page
   :about #'about-page
   :projects #'projects-page})

(defn page []
  [(pages (session/get :page))])


;; Routes ----------------------------------------------------------------------
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :page :home))

(secretary/defroute "/about" []
  (session/put! :page :about))

(secretary/defroute "/projects" []
  (session/put! :page :projects))


;; History ---------------------------------------------------------------------
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))


;; Initialize ------------------------------------------------------------------
(defn fetch-docs! []
  (GET "/docs" {:handler #(session/put! :docs %)}))

(defn mount-components []
  (reagent/render-component [#'navbar] (.getElementById js/document "navbar"))
  (reagent/render-component [#'page] (.getElementById js/document "app")))

(defn init! []
  (fetch-docs!)
  (hook-browser-navigation!)
  (session/put! :page :home)
  (mount-components))
