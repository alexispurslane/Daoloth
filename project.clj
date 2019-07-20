(defproject daoleth "0.1.0-SNAPSHOT"
  :description "level editor for the lovecraftean catch 'em all roguelike by @pimpale"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [seesaw "1.5.1-SNAPSHOT"]]
  :main ^:skip-aot daoleth.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
