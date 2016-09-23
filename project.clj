(defproject ics "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.89"]
                 [reagent "0.6.0-rc"]
                 [binaryage/devtools "0.6.1"]
                 [re-frame "0.8.0"]
                 [cljs-ajax "0.5.8"]
                 [secretary "1.2.3"]
                 [cljsjs/highcharts "4.2.6-0"] ; no use ?!
                 [cljsjs/reactable "0.12.5-0"]
                 [cljsjs/nprogress "0.2.0-1"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 ]

  :plugins [[lein-cljsbuild "1.1.3"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :profiles
  {:dev
   {:dependencies []

    :plugins      [[lein-figwheel "0.5.4-3"]]
    }}

  :cljsbuild
  {
   :builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "ics.core/mount-root"}
     :compiler     {:main                 ics.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :externs              ["externs.js"]
                    :source-map-timestamp true}}

    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            ics.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :externs         ["resources/public/js/compiled/externs.js"]
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}

    ]}

  )
