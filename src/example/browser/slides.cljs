(ns example.browser.slides
  (:import goog.History)
  (:require
   [goog.history.EventType :as EventType]
   [goog.events :as events]
   [goog.dom :as dom]
   [nuid.browser.viewport :as viewport]
   [nuid.browser.styles :as styles]
   [nuid.browser.scroll :as scroll]
   [stylefy.core :as css]
   [reagent.core :as r]
   [bidi.bidi :as bidi]
   [example.browser.login :as login]))

(def routes ["/"])
(def match-route (partial bidi/match-route routes))
(defonce route (r/atom nil))
(def route! (comp (partial reset! route) match-route))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [e] (route! (.-token e))))
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
          :margin  0
          :padding 0})

(defn slide
  [{:keys [index]} content]
  [:div
   (css/use-style
    {:height   "100vh"
     :overflow "hidden"
     :width    "100vw"}
    {:id index
     :on-click
     (fn [e]
       (->>
        (if (< (.-clientY e)
               (/ (viewport/height) 2))
          (dec index)
          (inc index))
        (str)
        (scroll/to {:duration-ms 500})))})
   content])

(def subtitle
  {:font-size   "1.5rem"
   :font-weight "300"
   :line-height "2rem"})

(defn about-us
  []
  [:div
   (css/use-style
    (merge
     styles/flex
     styles/align-center
     styles/vertical-center
     {:flex-direction "column"
      :height         "100%"}))

   [:div
    (css/use-style
     {:width "50%"})

    [:h1 "About NuID"]

    [:h3
     (css/use-style
      subtitle)
     "NuID is a new paradigm in digital identity and authentication. By combining DLT and zero knowledge cryptography, we enable developers to get rid of centralized credential databases, embrace a decentralized identity model, and return ownership of authentication data to where it belongs: with users."]]])

(defn breach-stats
  []
  [:div
   (css/use-style
    (merge
     styles/flex
     styles/align-center
     styles/space-around
     {:height  "100%"
      :padding "0 5vw"}))

   [:h1
    (css/use-style
     {:font-weight "300"})
    "Password storage causes problems."]

   (let [div-style
         {:margin "33px 0"}
         statistic-style
         {:font-size "4rem"
          :margin    "0"}
         explanation-style
         (merge
          {:font-weight "300"
           :margin      "0"})]

     [:div
      [:div
       (css/use-style
        div-style)

       [:h1
        (css/use-style
         statistic-style)
        "$7.4M"]

       [:h1
        (css/use-style
         explanation-style)
        "Average cost of data breach"]]

      [:div
       (css/use-style
        div-style)

       [:h1
        (css/use-style
         statistic-style)
        "3.2B"]

       [:h1
        (css/use-style
         explanation-style)
        "Passwords compromised in 2016"]]

      [:div
       (css/use-style
        div-style)

       [:h1
        (css/use-style
         statistic-style)
        "80%"]

       [:h1
        (css/use-style
         explanation-style)
        "Attacks use stolen or weak passwords"]]])])

(defn centralized-paradigm
  []
  [:div
   (css/use-style
    (merge
     styles/flex
     styles/align-center
     styles/vertical-center
     {:flex-direction "column"
      :height         "100%"}))

   [:img {:src "/svg/centralized-paradigm.svg"}]

   [:h1
    (css/use-style
     {:font-weight "300"
      :margin-top  "115px"
      :text-align  "center"})
    "Walled gardens cause problems."]])

(defn federated-paradigm
  []
  [:div
   (css/use-style
    (merge
     styles/flex
     styles/align-center
     styles/vertical-center
     {:flex-direction "column"
      :height         "100%"}))

   [:img {:src "/svg/federated-paradigm.svg"}]

   [:h1
    (css/use-style
     {:font-weight "300"
      :margin-top  "115px"
      :text-align  "center"})
    "Federation helps..."]])

(defn decentralized-paradigm
  []
  [:div
   (css/use-style
    (merge
     styles/flex
     styles/align-center
     styles/vertical-center
     {:flex-direction "column"
      :height         "100%"}))

   [:img {:src "/svg/decentralized-paradigm.svg"}]

   [:h1
    (css/use-style
     {:font-weight "300"
      :margin-top  "115px"
      :text-align  "center"})
    "NuID simplifies identity management with transparent key coordination."]])

(defn asymmetric-authentication
  []
  [:div
   (css/use-style
    (merge
     styles/flex
     styles/align-center
     styles/vertical-center
     {:height "100%"}))

   [:div
    (css/use-style
     (merge
      styles/flex
      styles/align-center
      {:justify-content "right"
       :height          "100%"
       :width           "50%"}))

    [:h1
     (css/use-style
      {:margin-right "10px"})
     "Asymmetric"]]

   [:div
    (css/use-style
     (merge
      styles/flex
      styles/align-center
      {:background "black"
       :color      "white"
       :height     "100%"
       :width      "50%"}))

    [:h1
     (css/use-style
      {:margin-left "10px"})
     "Authentication"]]])

(defn zero-knowledge
  []
  [:div
   (css/use-style
    (merge
     styles/flex
     styles/align-center
     styles/vertical-center
     {:flex-direction "column"
      :height         "100%"
      :position       "relative"}))

   [:div
    (css/use-style
     {:width "50%"})

    [:h1 "Zero Knowledge Proofs"]

    [:h3
     (css/use-style
      (merge
       subtitle
       {:font-style  "italic"
        :font-weight "200"}))
     "\"Zero Knowledge schemes allow a prover (user) to prove knowledge of a secret while not revealing any information about the secret itself to the verifying server.\""

     [:sup
      (css/use-style
       {:font-size   "1rem"
        :line-height "0"})
      "1"]]

    [:h3
     (css/use-style
      subtitle)
     "Zero knowledge proofs are similar to other asymmetric schemes, e.g. proof of possession."]

    [:h3
     (css/use-style
      subtitle)
     "Nothing sensitive ever leaves the device."]

    [:p
     (css/use-style
      {:bottom      "5vh"
       :font-size   "1rem"
       :font-style  "italic"
       :font-weight "200"
       :line-height "0"
       :position    "absolute"})
     [:sup "1"]
     "Professor Matthew Green, Johns Hopkins University"]]])

(defn zk+dlt
  []
  [:div
   (css/use-style
    (merge
     styles/flex
     styles/align-center
     styles/vertical-center
     {:height "100%"}))

   [:div
    [:h1
     [:span
      (css/use-style
       {:font-weight "300"})
      "ZK + DLT = "]
     "NuID"]]

   [:div
    (css/use-style
     {:margin "0 0 0 5vw"})

    [:h1
     (css/use-style
      {:font-weight "300"})
     "Authentication"]

    [:h1
     (css/use-style
      {:font-weight "300"})
     "Key Management"]

    [:h1
     (css/use-style
      {:font-weight "300"})
     "SSO"]]])

(defn nuid+daml-use-cases
  []
  [:div
   (css/use-style
    (merge
     styles/flex
     {:height "100%"}))

   [:div
    (css/use-style
     {:width "50%"})

    [:div
     (css/use-style
      {:background          "url(/png/daml.png)"
       :background-size     "cover"
       :background-position "0%"
       :height              "50%"})]

    [:video
     (css/use-style
      {:background "black"
       :height     "50%"
       :max-width  "100%"}
      {:autoPlay true
       :loop     true
       :muted    true})

     [:source
      {:src  "/mp4/logo-animation.mp4"
       :type "video/mp4"}]]]

   [:div
    (css/use-style
     (merge
      styles/flex
      styles/align-center
      styles/vertical-center
      {:width "50%"}))

    (let [value-prop-style
          {:margin "11px 0"}]

      [:div
       [:h1
        (css/use-style
         {:font-weight "300"
          :margin      "0"})
        "Use cases"]

       [:h1
        (css/use-style
         value-prop-style)
        "Reusable KYC"]

       [:h1
        (css/use-style
         value-prop-style)
        "Enterprise SSO"]

       [:h1
        (css/use-style
         value-prop-style)
        "ID Management"]

       [:h1
        (css/use-style
         value-prop-style)
        "Modern Login"]])]])

(defn nuid+daml-developer-benefits
  []
  [:div
   (css/use-style
    (merge
     styles/flex
     {:height "100%"}))

   [:div
    (css/use-style
     {:height "100%"
      :width  "50%"})

    [:div
     (css/use-style
      {:background          "url(/png/daml.png)"
       :background-size     "cover"
       :background-position "0%"
       :height              "50%"})]

    [:video
     (css/use-style
      {:background "black"
       :height     "50%"
       :max-width  "100%"}
      {:autoPlay true
       :loop     true
       :muted    true})

     [:source
      {:src  "/mp4/logo-animation.mp4"
       :type "video/mp4"}]]]

   [:div
    (css/use-style
     (merge
      styles/flex
      styles/align-center
      styles/vertical-center
      {:width "50%"}))

    (let [value-prop-style
          {:margin "25px 0"}]

      [:div
       [:h1
        (css/use-style
         value-prop-style)
        "+ Ledger Abstraction"]

       [:h1
        (css/use-style
         value-prop-style)
        "+ Developer Tooling"]

       [:h1
        (css/use-style
         value-prop-style)
        "+ Enterprise Integration"]])]])

(defn consortium-example
  []
  [:div
   (css/use-style
    (merge
     styles/flex
     styles/align-center
     styles/vertical-center
     {:flex-direction "column"
      :height         "100%"}))

   [:img {:src "/svg/consortium.svg"}]

   [:div
    [:h1
     (css/use-style
      {:font-weight "300"
       :margin      "70px 0 0"})
     "Consortium Example"]

    [:h1
     (css/use-style
      {:margin "0"})
     "Secure applications for trading digital assets."]]])

(def slides
  [about-us
   breach-stats
   centralized-paradigm
   federated-paradigm
   decentralized-paradigm
   asymmetric-authentication
   zero-knowledge
   zk+dlt
   nuid+daml-use-cases])

(defn demo-start
  []
  [:div
   (css/use-style
    (merge
     styles/flex
     styles/align-center
     styles/vertical-center
     {:height "100vh"})
    {:id (inc (count slides))})

   [login/component
    {:register-fn     (partial login/register! {:endpoint js/window.origin})
     :authenticate-fn (partial login/authenticate! {:endpoint js/window.origin})}]])

(defn deck
  []
  [:div
   (doall
    (map-indexed
     (fn [i s]
       ^{:key i}
       [slide {:index (inc i)}
        [s]])
     slides))
   [demo-start]])

(defn content
  []
  [deck])

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
