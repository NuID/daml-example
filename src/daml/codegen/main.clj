(ns daml.codegen.main
  (:import
   [com.digitalasset.daml.lf.codegen Main]))

(defn -main [& args]
  (Main/main (into-array String args)))
