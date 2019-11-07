(ns example.browser.login
  (:require
   [clojure.core.async :as async :include-macros true]
   [clojure.spec.alpha :as s]
   [nuid.zk :as zk]
   [cljs-http.client :as http]
   [stylefy.core :as css]
   [reagent.core :as r]
   [goog.dom :as dom]
   [example.utils :as utils]))

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

(def background-color-ratom (r/atom "white"))
(def credential-ratom (r/atom {}))

(defn shift-background-color!
  [to & [{:keys [timeout-ms final]
          :or   {timeout-ms 500
                 final      "white"}}]]
  (reset! background-color-ratom to)
  (js/setTimeout
   (fn [] (reset! background-color-ratom final))
   timeout-ms))

(defn initialize!
  [{:keys [endpoint]}
   {:keys [id]}]
  (http/get (str endpoint "/initialize")
            {:query-params {"id" id}}))

(defn prove!
  [{:keys [endpoint]}
   {:keys [id secret]}
   parameters]
  (let [challenge
        (->>
         (merge
          (utils/generate-default-parameters)
          parameters
          {:id id})
         (s/conform ::zk/challenge))]
    (->>
     (assoc challenge :secret secret)
     (zk/proof)
     (merge challenge)
     (s/conform ::zk/proof)
     (s/unform ::zk/proof)
     (assoc {} :json-params)
     (http/post (str endpoint "/verify")))))

(defn authenticate!
  [{:keys [endpoint] :as opts} cred]
  (async/go
    (let [resp (async/<! (initialize! opts cred))]
      (if (not (= 200 (:status resp)))
        (swap! credential-ratom assoc :confirm "")
        (let [resp (async/<! (prove! opts cred (:body resp)))]
          (if-let [token (:token (:body resp))]
            (js/location.assign (str endpoint "?token=" token))
            (shift-background-color! "#ff2900")))))))

(defn register!
  [{:keys [endpoint]}
   {:keys [id secret]
    :as   cred}]
  (if (= (:secret cred) (:confirm cred))
    (async/go
      (let [resp (->>
                  (utils/generate-proof {:secret secret})
                  (merge {:id id})
                  (s/conform ::zk/proof)
                  (s/unform ::zk/proof)
                  (assoc {} :json-params)
                  (http/post (str endpoint "/register"))
                  (async/<!))]
        (if-let [token (:token (:body resp))]
          (js/location.assign (str endpoint "?token=" token))
          (shift-background-color! "#ff2900"))))
    (shift-background-color! "#ff2900")))

(def input-style
  {:background    "transparent"
   :border-bottom "1px solid black"
   :border-left   "none"
   :border-right  "none"
   :border-top    "none"
   :font-size     "1.5rem"
   :padding       "2px 0 5px"})

(def label-style
  {:display     "block"
   :font-size   "1.5rem"
   :font-weight "200"})

(def button-style
  {:background    "transparent"
   :border        "1px solid transparent"
   :border-radius "2px"
   :box-sizing    "border-box"
   :font-size     "1.5rem"
   :font-weight   "400"
   :padding       "5px 10px"
   ::css/mode
   (let [state {:border "1px solid black"}]
     {:active state
      :focus  state
      :hover  state})})

(defn component
  [{:keys [register-fn authenticate-fn]}]
  [:form
   {:style {:margin "0"}}

   [:fieldset
    {:style {:border "none"}}

    [:legend {:hidden true} "login box"]

    [:div
     [:label
      (css/use-style
       label-style
       {:for "email"})
      "email"]

     [:input#email
      (css/use-style
       input-style
       {:auto-focus (not (= js/window.location.pathname "/slides"))
        :type       "email"
        :value      (:id @credential-ratom)
        :on-change  (fn [e] (swap! credential-ratom assoc :id (.. e -target -value)))})]]

    [:div
     {:style {:margin-top "20px"}}
     [:label
      (css/use-style
       label-style
       {:for "secret"})
      "password"]

     [:input#secret
      (css/use-style
       input-style
       {:type      "password"
        :value     (:secret @credential-ratom)
        :on-change (fn [e] (swap! credential-ratom assoc :secret (.. e -target -value)))})]]

    (when (:confirm @credential-ratom)
      [:div
       {:style {:margin-top "20px"}}

       [:label
        (css/use-style
         label-style
         {:for "confirm"})
        "confirm"]

       [:input#confirm
        (css/use-style
         input-style
         {:auto-focus true
          :on-change  (fn [e] (swap! credential-ratom assoc :confirm (.. e -target -value)))
          :type       "password"
          :value      (:confirm @credential-ratom)})]])

    [:div
     (css/use-style
      {:display         "flex"
       :justify-content "space-around"
       :margin-top      "20px"})

     [:button
      (css/use-style
       button-style
       {:on-click
        (fn [e]
          (if (:confirm @credential-ratom)
            (register-fn @credential-ratom)
            (authenticate-fn @credential-ratom))
          (.preventDefault e))
        :type "button"})
      (if (:confirm @credential-ratom)
        "register"
        "login")]

     (when (not (every? empty? (vals @credential-ratom)))
       [:button
        (css/use-style
         button-style
         {:on-click (fn [] (reset! credential-ratom nil))
          :type     "reset"})
        "clear"])]]])

(defn content
  []
  [:div
   (css/use-style
    {:align-items      "center"
     :background-color @background-color-ratom
     :display          "flex"
     :height           "100vh"
     :justify-content  "center"
     :width            "100vw"})

   [component
    {:register-fn     (partial register! {:endpoint js/window.origin})
     :authenticate-fn (partial authenticate! {:endpoint js/window.origin})}]])

(defn render
  []
  (r/render
   [content]
   (dom/getElement "app")))

(defn ^:export init
  []
  (css/init global-css)
  (render))
