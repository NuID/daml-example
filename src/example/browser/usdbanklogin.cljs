(ns example.browser.usdbanklogin
  (:require
   [stylefy.core :as css]
   [reagent.core :as r]
   [goog.dom :as dom]
   [example.browser.login :as login]))

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
  []
  [:div
   (css/use-style
    {:align-items     "center"
     :background      "linear-gradient(45deg,#354c86,#2b3c68)"
     :color           "white"
     :display         "flex"
     :height          "100vh"
     :justify-content "center"
     :width           "100vw"})

   [login/component
    {:register-fn
     (partial login/register! {:endpoint (str js/window.origin "/usdbank")})
     :authenticate-fn
     (partial login/authenticate! {:endpoint (str js/window.origin "/usdbank")})}]])

(defn render
  []
  (r/render
   [content]
   (dom/getElement "app")))

(defn ^:export init
  [_]
  (css/init global-css)
  (render))
