(ns example.browser.slides
  (:require
   [clojure.core.async :as async :include-macros true]
   [nuid.browser.viewport :as viewport]
   [nuid.browser.styles :as styles]
   [nuid.browser.scroll :as scroll]
   [example.browser.login :as login]
   [example.utils :as utils]
   [stylefy.core :as css]
   [reagent.core :as r]
   [goog.dom :as dom]))



(defn slide
  [{:keys [index] :as props} content]
  [:div
   (css/use-style
    {:height "100vh"
     :width "100vw"}
    {:id index
     :on-click #(if (< (.-clientY %)
                       (/ (viewport/height) 2))
                  (scroll/to (str (dec index)) :duration 500)
                  (scroll/to (str (inc index)) :duration 500))})
   content])

(def subtitle
  {:font-size "1.5rem"
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
      :height "100%"}))
   [:div
    (css/use-style
     {:width "50%"})
    [:h1 "About NuID"]
    [:h3
     (css/use-style
      subtitle)
     "NuID is a new paradigm in digital identity and authentication. By combining DLT and zero knowledge cryptography, we enable enterprises to get rid of centralized credential databases, embrace a decentralized identity model, and return ownership of authentication data where it belongs: with their users."]]])

(defn breach-stats
  []
  [:div
   (css/use-style
    (merge
     styles/flex
     styles/align-center
     styles/space-around
     {:height "100%"
      :padding "0 5vw"}))
   [:h1
    (css/use-style
     {:font-weight "300"})
    "Password storage causes problems."]
   (let [div-style
         {:margin "33px 0"}
         statistic-style
         {:font-size "4rem"
          :margin "0"}
         explanation-style
         (merge
          {:font-weight "300"
           :margin "0"})]
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
      :height "100%"}))
   [:img {:src "/svg/centralized-paradigm.svg"}]
   [:h1
    (css/use-style
     {:font-weight "300"
      :margin-top "115px"})
    "Walled gardens cause problems."]])

(defn decentralized-paradigm
  []
  [:div
   (css/use-style
    (merge
     styles/flex
     styles/align-center
     styles/vertical-center
     {:flex-direction "column"
      :height "100%"}))
   [:img {:src "/svg/decentralized-paradigm.svg"}]
   [:h1
    (css/use-style
     {:font-weight "300"
      :margin-top "115px"})
    "NuID simplifies identity management with transparent key coordination."]])

(defn zero-knowledge
  []
  [:div
   (css/use-style
    (merge
     styles/flex
     styles/align-center
     styles/vertical-center
     {:flex-direction "column"
      :height "100%"
      :position "relative"}))
   [:div
    (css/use-style
     {:width "50%"})
    [:h1 "Zero Knowledge Proofs"]
    [:h3
     (css/use-style
      (merge
       subtitle
       {:font-style "italic"
        :font-weight "200"}))
     "\"Zero Knowledge schemes allow a prover (user) to prove knowledge of a secret while not revealing any information about the secret itself to the verifying server.\""
     [:sup
      (css/use-style
       {:font-size "1rem"
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
      {:bottom "5vh"
       :font-size "1rem"
       :font-style "italic"
       :font-weight "200"
       :line-height "0"
       :position "absolute"})
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

(defn nuid+daml
  []
  [:div
   (css/use-style
    styles/flex
    {:height "100%"})
   [:div
    (css/use-style
     {:width "50%"})
    [:div
     (css/use-style
      {:background "url(/png/daml.png)"
       :background-size "cover"
       :background-position "0%"
       :height "53vh"})]
    [:video
     (css/use-style
      {:max-width "100%"}
      {:autoPlay true
       :loop true
       :muted true})
     [:source
      {:src "/mp4/logo-animation.mp4"
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
          :margin "0"})
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

(defn consortium-example
  []
  [:div
   (css/use-style
    (merge
     styles/flex
     styles/align-center
     styles/vertical-center
     {:flex-direction "column"
      :height "100%"}))
   [:img {:src "/svg/consortium.svg"}]
   [:div
    [:h1
     (css/use-style
      {:font-weight "300"
       :margin "70px 0 0"})
     "Consortium Example"]
    [:h1
     (css/use-style
      {:margin "0"})
     "Secure applications for trading digital assets."]]])

(def slides
  [about-us
   breach-stats
   centralized-paradigm
   decentralized-paradigm
   zero-knowledge
   zk+dlt
   nuid+daml
   consortium-example])

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
    {:register-fn (partial login/register! {:endpoint "/"})
     :authenticate-fn (partial login/authenticate! {:endpoint "/"})}]])

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
