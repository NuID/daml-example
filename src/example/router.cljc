(ns example.router
  (:require
   [example.handlers :as handlers]
   [bidi.ring :as bidi.ring]
   [bidi.bidi :as bidi]))

(def routes
  ["/"
   [[:get           handlers/home]
    ["register"     [[:post handlers/register]]]
    ["initialize"   [[:get handlers/initialize]]]
    ["verify"       [[:post handlers/verify]]]
    ["presentation" [[:get handlers/presentation]]]
    ["usdbank"
     [[:get          handlers/usdbank]
      ["/register"   [[:post handlers/register]]]
      ["/initialize" [[:get handlers/initialize]]]
      ["/verify"     [[:post handlers/verify]]]]]]])

(def handler
  (bidi.ring/make-handler routes))
