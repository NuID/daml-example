(ns example.browser.home
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
          :margin 0
          :padding 0})

(css/tag "button" {::css/mode {:focus {:outline "0"}}})

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
  [id]
  [:div
   (css/use-style
    {:align-items "center"
     :display "flex"
     :flex-direction "column"
     :height "100vh"
     :justify-content "center"
     :width "100vw"})
   [:div
    [:h1
     {:style
      {:color "lightgray"
       :margin "0"
       :padding "0"}}
     "home"]
    [:h1.rainbowed
     {:style
      {:font-family "Favorit"
       :margin "-5px 0"
       :padding "0"}}
     id]]])

(defn render
  [id]
  (r/render
   [content id]
   (dom/getElement "app")))

(defn ^:export init
  [& [id]]
  (css/init global-css)
  (render id))
