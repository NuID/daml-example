(ns example.browser.usdbank
  (:require
   [stylefy.core :as css]
   [reagent.core :as r]
   [goog.dom :as dom]))

(def global-css
  {:global-vendor-prefixes
   {::css/vendors ["webkit" "moz" "ms" "o"]
    ::css/auto-prefix
    #{:align-items
      :box-sizing
      :box-shadow
      :flex
      :flex-direction
      :flex-grow
      :flex-wrap
      :justify-content
      :transition
      :transform}}})

(css/tag "body"
         {:font-family
          (clojure.string/join
           ", "
           ["\"Helvetica Neue\""
            "Helvetica"
            "-apple-system"
            "BlinkMacSystemFont"
            "\"Segoe UI\""
            "Roboto"
            "Oxygen"
            "Ubuntu"
            "Cantarell"
            "\"Open Sans\""
            "sans-serif"])
          :margin  0
          :padding 0})

(defn content
  [id]
  [:div
   (css/use-style
    {:align-items     "center"
     :background      "linear-gradient(45deg,#354c86,#2b3c68)"
     :color           "white"
     :display         "flex"
     :height          "100vh"
     :justify-content "center"
     :width           "100vw"})
   [:div
    [:h1
     (css/use-style
      {:color  "black"
       :margin "0"})
     "USD Bank"]
    [:h1
     (css/use-style
      {:font-family "Favorit"
       :margin      "0"
       :text-shadow "2px 2px black"})
     id]]])

(defn render
  [id]
  (r/render
   [content id]
   (dom/getElement "app")))

(defn ^:export init
  [id]
  (css/init global-css)
  (render id))
