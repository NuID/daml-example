(ns example.browser.login
  (:require
   [clojure.core.async :as async :include-macros true]
   [nuid.elliptic.curve.point :as point]
   [nuid.transit :as transit]
   [nuid.zk :as zk]
   [nuid.bn :as bn]
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
          :margin 0
          :padding 0})

(def transit-read-opts
  {:handlers (merge point/transit-read-handler
                    bn/transit-read-handler)})
(def transit-write-opts
  {:handlers (merge point/transit-write-handler
                    bn/transit-write-handler)})
(def transit-write (partial transit/write transit-write-opts))
(def transit-read (partial transit/read transit-read-opts))
(def background-color (r/atom "white"))
(def credential (r/atom {}))

(defn shift-background-color!
  [to & [{:keys [timeout-ms final]
          :or {timeout-ms 500
               final "white"}}]]
  (reset! background-color to)
  (js/setTimeout #(reset! background-color final)
                 timeout-ms))

(defn initialize!
  [{:keys [endpoint] :as opts} {id :id}]
  (http/get (str endpoint "/initialize")
            {:query-params {"id" id}
             :transit-opts {:decoding-opts transit-read-opts}}))

(defn prove!
  [{:keys [endpoint] :as opts} {:keys [id secret]} parameters]
  (let [cred (merge utils/default-spec parameters {:id id})
        proof (->> (assoc cred :secret secret)
                   (zk/coerce)
                   (zk/proof)
                   (merge cred))]
    (http/post (str endpoint "/verify")
               {:transit-params proof
                :transit-opts {:encoding-opts transit-write-opts}})))

(defn authenticate!
  [{:keys [endpoint] :as opts} cred]
  (async/go
    (let [resp (async/<! (initialize! opts cred))]
      (if (not (= 200 (:status resp)))
        (swap! credential assoc :confirm "")
        (let [resp (async/<! (prove! opts cred (:body resp)))]
          (if-let [token (:token (:body resp))]
            (js/location.assign (str endpoint "?token=" token))
            (shift-background-color! "#ff2900")))))))

(defn register!
  [{:keys [endpoint] :as opts} cred]
  (if (= (:secret cred) (:confirm cred))
    (async/go
      (let [{:keys [id secret]} cred
            params (merge {:id id} (utils/create-proof {:secret secret}))
            resp (async/<! (http/post (str endpoint "/register")
                                      {:transit-params params
                                       :transit-opts
                                       {:encoding-opts transit-write-opts
                                        :decoding-opts transit-read-opts}}))]
        (if-let [token (:token (:body resp))]
          (js/location.assign (str endpoint "?token=" token))
          (shift-background-color! "#ff2900"))))
    (shift-background-color! "#ff2900")))

(def input-style
  {:background "transparent"
   :border-bottom "1px solid black"
   :border-left "none"
   :border-right "none"
   :border-top "none"
   :font-size "1.5rem"
   :padding "2px 0 5px"})

(def label-style
  {:display "block"
   :font-size "1.5rem"
   :font-weight "200"})

(def button-style
  {:background "transparent"
   :border "1px solid transparent"
   :border-radius "2px"
   :box-sizing "border-box"
   :font-size "1.5rem"
   :font-weight "400"
   :padding "5px 10px"
   ::css/mode
   (let [state {:border "1px solid black"}]
     {:active state
      :focus state
      :hover state})})

(defn component
  [{:keys [register-fn authenticate-fn]}]
  [:form
   {:style {:margin "0"}}
   [:fieldset
    {:style {:border "none"}}
    [:legend
     {:hidden true}
     "login box"]
    [:div
     [:label
      (css/use-style
       label-style
       {:for "email"})
      "email"]
     [:input#email
      (css/use-style
       input-style
       {:type "email"
        :value (:id @credential)
        :on-change #(swap! credential assoc :id (.-value (.-target %)))})]]
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
       {:type "password"
        :value (:secret @credential)
        :on-change #(swap! credential assoc :secret (.-value (.-target %)))})]]
    (when (:confirm @credential)
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
          :type "password"
          :value (:confirm @credential)
          :on-change #(swap! credential assoc :confirm (.-value (.-target %)))})]])
    [:div
     (css/use-style
      {:display "flex"
       :justify-content "space-around"
       :margin-top "20px"})
     [:button
      (css/use-style
       button-style
       {:on-click #(if (:confirm @credential)
                     (register-fn @credential)
                     (authenticate-fn @credential))
        :type "button"})
      (if (:confirm @credential)
        "register"
        "login")]
     (when (not (every? empty? (vals @credential)))
       [:button
        (css/use-style
         button-style
         {:on-click #(reset! credential nil)
          :type "reset"})
        "clear"])]]])

(defn content
  []
  [:div
   (css/use-style
    {:align-items "center"
     :background-color @background-color
     :display "flex"
     :height "100vh"
     :justify-content "center"
     :width "100vw"})
   [component
    {:register-fn (partial register! {:endpoint "/"})
     :authenticate-fn (partial authenticate! {:endpoint "/"})}]])

(defn render
  []
  (r/render
   [content]
   (dom/getElement "app")))

(defn ^:export init
  []
  (css/init global-css)
  (render))
