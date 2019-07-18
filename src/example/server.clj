(ns example.server
  (:require
   [nuid.elliptic.curve.point :as point]
   [nuid.transit :as transit]
   [nuid.bn :as bn]
   [buddy.auth.middleware :as buddy.middleware]
   [buddy.auth.backends.token :as buddy.token]
   [ring.middleware.keyword-params :as ring.keyword-params]
   [ring.middleware.content-type :as ring.content-type]
   [ring.middleware.not-modified :as ring.not-modified]
   [ring.middleware.resource :as ring.resource]
   [ring.middleware.params :as ring.params]
   [ring.middleware.format-response :as ring.format.response]
   [ring.middleware.format-params :as ring.format.params]
   [ring.middleware.format :as ring.format]
   [org.httpkit.server :as http]
   [example.router :as router]
   [example.daml :as daml]))

(defonce server-atom (atom nil))

(def secret "test")
(def auth-backend
  (buddy.token/jws-backend
   {:secret secret
    :options {:alg :hs256}}))

(def transit-read-opts
  {:handlers (merge point/transit-read-handler
                    bn/transit-read-handler)})

(def transit-write-opts
  {:handlers (merge point/transit-write-handler
                    bn/transit-write-handler)})

(defn wrap-token-param
  [handler]
  (fn [req]
    (handler
     (if-let [token (get-in req [:params :token])]
       (update req :headers assoc "Authorization" (str "Token " token))
       req))))

(def app
  (-> router/handler
      (buddy.middleware/wrap-authorization auth-backend)
      (buddy.middleware/wrap-authentication auth-backend)
      (wrap-token-param)
      (ring.format.params/wrap-restful-params
       {:format-options
        {:transit-json transit-read-opts}})
      (ring.format.response/wrap-restful-response
       {:format-options
        {:transit-json transit-write-opts}})
      (ring.keyword-params/wrap-keyword-params)
      (ring.params/wrap-params)
      (ring.resource/wrap-resource "public")
      (ring.content-type/wrap-content-type)
      (ring.not-modified/wrap-not-modified)))

(defn start
  [& [{:keys [handler port]
       :or {handler app
            port 8080}}]]
  (daml/start)
  (->> (http/run-server handler {:port port})
       (reset! server-atom)))

(defn stop
  []
  (when (not (nil? @server-atom))
    (daml/stop)
    (@server-atom :timeout 100)
    (reset! server-atom nil)))

(defn -main
  [& args]
  (start))

(comment

  (require '[example.server :as server] :reload-all) ; this file
  (server/start)
  (server/stop)

  (require '[example.daml :as daml] :reload-all)
  (require '[example.handlers :as handlers])

  )
