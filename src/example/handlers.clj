(ns example.handlers
  (:require
   [clojure.spec-alpha2.gen :as gen]
   [clojure.spec-alpha2 :as s]
   [nuid.cryptography :as crypt]
   [nuid.zk :as zk]
   [ring.util.response :as ring.response]
   [buddy.auth :as buddy.auth]
   [buddy.sign.jwt :as buddy.jwt]
   [hiccup.core :as hiccup]
   [example.daml :as daml]
   [example.utils :as utils]))

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
  [{:keys [id]}]
  [:div
   [:div {:id "app"}]
   [:script {:src "js/example/home/home.js"}]
   [:script (str "example.browser.home.init(\"" id "\")")]])

(def slides-app-hiccup
  [:div
   [:div {:id "app"}]
   [:script {:src "js/example/slides/slides.js"}]
   [:script (str "example.browser.slides.init()")]])

(def usdbank-login-app-hiccup
  [:div
   [:div {:id "app"}]
   [:script {:src "js/example/usdbanklogin/usdbanklogin.js"}]
   [:script (str "example.browser.usdbanklogin.init()")]])

(defn usdbank-app-hiccup
  [{:keys [id]}]
  [:div
   [:div {:id "app"}]
   [:script {:src "js/example/usdbank/usdbank.js"}]
   [:script (str "example.browser.usdbank.init(\"" id "\")")]])

(defn home
  [req]
  (->
   [:html default-head-hiccup
    [:body
     (if (buddy.auth/authenticated? req)
       (home-app-hiccup (:identity req))
       login-app-hiccup)]]
   (hiccup/html)
   (ring.response/response)
   (ring.response/content-type "text/html")))

(defn slides
  [_]
  (->
   [:html default-head-hiccup
    [:body slides-app-hiccup]]
   (hiccup/html)
   (ring.response/response)
   (ring.response/content-type "text/html")))

(defn usdbank
  [req]
  (->
   [:html default-head-hiccup
    [:body
     (if (buddy.auth/authenticated? req)
       (usdbank-app-hiccup (:identity req))
       usdbank-login-app-hiccup)]]
   (hiccup/html)
   (ring.response/response)
   (ring.response/content-type "text/html")))

(def nonce-atom (atom #{}))

(defn register
  [req]
  (let [proof (get-in req [:params])]
    (if (s/valid? ::zk/proof proof)
      (let [claims (select-keys proof [:id])
            token  (buddy.jwt/sign claims secret {:alg :hs256})]
        (daml/submit! (select-keys proof [:id :keyfn :pub]))
        (->
         {:token token}
         (ring.response/response)
         (ring.response/content-type "application/json")))
      {:status 422})))

(defn initialize
  [req]
  (let [id (get-in req [:params :id])]
    (if-let [credential (@daml/credentials-atom id)]
      (let [challenge (utils/generate-challenge credential)]
        (swap! nonce-atom conj (:nonce challenge))
        (->
         challenge
         (ring.response/response)
         (ring.response/content-type "application/json")))
      {:status 404})))

(defn verify
  [req]
  (let [proof (get-in req [:params])]
    (if (and (contains? @nonce-atom (:nonce proof))
             (s/valid? ::zk/proof proof))
      (let [claims (select-keys proof [:id])
            token  (buddy.jwt/sign claims secret {:alg :hs256})]
        (->
         {:token token}
         (ring.response/response)
         (ring.response/content-type "application/json")))
      {:status 422})))
