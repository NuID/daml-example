(ns example.browser.presentation
  (:import goog.History)
  (:require
   [clojure.core.async :as async :include-macros true]
   [example.browser.slides :as slides]
   [example.browser.login :as login]
   [example.utils :as utils]
   [nuid.transit :as transit]
   [stylefy.core :as css]
   [reagent.core :as r]
   [bidi.bidi :as bidi]
   [goog.history.EventType :as EventType]
   [goog.events :as events]
   [goog.dom :as dom]))

(def routes ["/"])
(def match-route (partial bidi/match-route routes))
(defonce route (r/atom nil))
(def route! (comp (partial reset! route) match-route))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     #(route! (.-token %)))
    (.setEnabled true)))

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
          :margin 0
          :padding 0})

(def colors ["#00fb00" "#ff2900" "#1b40ff" "#fff6e3"])
(def size (* 5 (count colors)))

(apply (partial css/keyframes "programmatic-rainbow")
       (map (fn [step]
              [(str (* (/ step size) 100.0) "%")
               {:text-shadow
                (->> (map (fn [px]
                            (let [i (-> (+ step px)
                                        (/ size)
                                        (* (count colors))
                                        (mod (count colors)))
                                  c (nth colors i)]
                              (str px "px " px "px " c)))
                          (range 1 size))
                     (clojure.string/join ", "))}])
            (range 0 (inc size))))

(css/tag ".rainbowed" {:animation "programmatic-rainbow 1s infinite"})

(defn content
  []
  [slides/deck])

(defn render
  []
  (r/render
   [content]
   (dom/getElement "app")))

(defn ^:export init
  []
  (css/init global-css)
  (hook-browser-navigation!)
  (render))
