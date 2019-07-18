(ns example.handlers
  (:require
   [nuid.elliptic.curve.point :as point]
   [nuid.cryptography :as crypt]
   [nuid.transit :as transit]
   [nuid.cbor :as nuid.cbor]
   [nuid.base64 :as base64]
   [nuid.zk :as zk]
   [nuid.bn :as bn]
   [ring.util.response :as ring.response]
   [buddy.auth :as buddy.auth]
   [buddy.sign.jwt :as buddy.jwt]
   [hiccup.core :as hiccup]
   [example.daml :as daml]
   [clj-cbor.core :as cbor]
   [clojure.walk :as walk]))

(def secret "test")

(def default-head-hiccup
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   [:style {:id "_stylefy-constant-styles_"}]
   [:style {:id "_stylefy-styles_"}]])

(def login-app-hiccup
  [:div
   [:div {:id "app"}]
   [:script {:src "js/example/login/login.js"}]
   [:script "example.browser.login.init()"]])

(defn home-app-hiccup
  [{:keys [id] :as ident}]
  [:div
   [:div {:id "app"}]
   [:script {:src "js/example/home/home.js"}]
   [:script (str "example.browser.home.init(\"" id "\")")]])

(def presentation-app-hiccup
  [:div
   [:div {:id "app"}]
   [:script {:src "js/example/presentation/presentation.js"}]
   [:script (str "example.browser.presentation.init()")]])

(def usdbank-login-app-hiccup
  [:div
   [:div {:id "app"}]
   [:script {:src "js/example/usdbanklogin/usdbanklogin.js"}]
   [:script (str "example.browser.usdbanklogin.init()")]])

(defn usdbank-app-hiccup
  [{:keys [id] :as ident}]
  [:div
   [:div {:id "app"}]
   [:script {:src "js/example/usdbank/usdbank.js"}]
   [:script (str "example.browser.usdbank.init(\"" id "\")")]])

(defn home
  [req]
  (-> [:html
       default-head-hiccup
       [:body
        (if (buddy.auth/authenticated? req)
          (home-app-hiccup (:identity req))
          login-app-hiccup)]]
      (hiccup/html)
      (ring.response/response)
      (ring.response/content-type "text/html")))

(defn presentation
  [req]
  (-> [:html
       default-head-hiccup
       [:body
        presentation-app-hiccup]]
      (hiccup/html)
      (ring.response/response)
      (ring.response/content-type "text/html")))

(defn usdbank
  [req]
  (-> [:html
       default-head-hiccup
       [:body
        (if (buddy.auth/authenticated? req)
          (usdbank-app-hiccup (:identity req))
          usdbank-login-app-hiccup)]]
      (hiccup/html)
      (ring.response/response)
      (ring.response/content-type "text/html")))

(def nonce-atom (atom #{}))

(def transit-read-opts
  {:handlers (merge point/transit-read-handler
                    bn/transit-read-handler)})
(def transit-read (partial transit/read transit-read-opts))
(def transit-write-opts
  {:handlers (merge point/transit-write-handler
                    bn/transit-write-handler)})
(def transit-write (partial transit/write transit-write-opts))

(defn register
  [req]
  (let [proof (get-in req [:params])]
    (if (zk/verified? (zk/coerce proof))
      (let [claims {:id (:id proof)}
            token (buddy.jwt/sign claims secret {:alg :hs256})]
        (daml/submit! (select-keys proof [:id :keyfn :pub]))
        (-> {:token token}
            (ring.response/response)
            (ring.response/content-type "application/transit+json")))
      {:status 422})))

(defn initialize
  [req]
  (let [id (get-in req [:params :id])]
    (if-let [credential (@daml/credentials-atom id)]
      (let [nonce (crypt/secure-random-bn 32)]
        (swap! nonce-atom conj nonce)
        (-> (merge credential {:nonce nonce})
            (transit-write)
            (ring.response/response)
            (ring.response/content-type "application/transit+json")))
      {:status 404})))

(defn verify
  [req]
  (let [proof (get-in req [:params])]
    (if (and (contains? @nonce-atom (:nonce proof))
             (zk/verified? (zk/coerce proof)))
      (let [claims {:id (:id proof)}
            token (buddy.jwt/sign claims secret {:alg :hs256})]
        (-> {:token token}
            (ring.response/response)
            (ring.response/content-type "application/transit+json")))
      {:status 422})))
