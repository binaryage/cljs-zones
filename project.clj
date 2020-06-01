(def clojurescript-version (or (System/getenv "CANARY_CLOJURESCRIPT_VERSION") "1.10.773"))
(defproject binaryage/zones "0.1.1-SNAPSHOT"
  :description "Async-aware binding & bound-fn."
  :url "https://github.com/binaryage/cljs-zones"
  :license {:name         "MIT License"
            :url          "http://opensource.org/licenses/MIT"
            :distribution :repo}

  :scm {:name "git"
        :url  "https://github.com/binaryage/cljs-zones"}

  :dependencies [[org.clojure/clojure "1.10.1" :scope "provided"]
                 [org.clojure/clojurescript ~clojurescript-version :scope "provided"]]

  :clean-targets ^{:protect false} ["target"
                                    "test/resources/.compiled"]

  :plugins [[lein-cljsbuild "1.1.8"]
            [lein-shell "0.5.0"]]

  :source-paths ["src/lib"]

  :test-paths ["test/src/tests"]

  :cljsbuild {:builds {}}                                                                                                     ; prevent https://github.com/emezeske/lein-cljsbuild/issues/413

  :profiles {:devel
                     {:cljsbuild {:builds {:devel
                                           {:source-paths ["src/lib"]
                                            :compiler     {:output-to     "target/devel/cljs_zones.js"
                                                           :output-dir    "target/devel"
                                                           :optimizations :none}}}}}

             :es2015 {:cljsbuild {:builds {:tests {:compiler {:external-config {:zones/config {:compilation-mode :ES2015}}}}}}}

             :testing
                     {:cljsbuild {:builds {:tests
                                           {:source-paths ["src/lib"
                                                           "test/src/tests"]
                                            :compiler     {:output-to     "test/resources/.compiled/tests/build.js"
                                                           :output-dir    "test/resources/.compiled/tests"
                                                           :asset-path    ".compiled/tests"
                                                           :main          zones.main
                                                           :optimizations :none}}}}}

             :testing-advanced
                     {:cljsbuild {:builds {:tests
                                           {:source-paths ["src/lib"
                                                           "test/src/tests"]
                                            :compiler     {:output-to     "test/resources/.compiled/tests-advanced/build.js"
                                                           :output-dir    "test/resources/.compiled/tests-advanced"
                                                           :asset-path    ".compiled/tests-advanced"
                                                           :main          zones.main
                                                           :optimizations :advanced}}}}}

             :auto-testing
                     {:cljsbuild {:builds {:tests
                                           {:notify-command ["node" "test/resources/puppeteer.js" "test/resources" "run-tests.html"]}}}}}


  :aliases {"test"                       ["do"
                                          "clean,"
                                          "test-tests,"
                                          "test-tests-advanced,"
                                          "clean,"
                                          "test-tests-es2015,"
                                          "test-tests-advanced-es2015,"
                                          ]
            "test-tests"                 ["do"
                                          "with-profile" "+testing" "cljsbuild" "once" "tests,"
                                          "shell" "node" "test/resources/puppeteer.js" "test/resources" "run-tests.html"]
            "test-tests-advanced"        ["do"
                                          "with-profile" "+testing-advanced" "cljsbuild" "once" "tests,"
                                          "shell" "node" "test/resources/puppeteer.js" "test/resources" "run-tests-advanced.html"]
            "test-tests-es2015"          ["do"
                                          "with-profile" "+testing,+es2015" "cljsbuild" "once" "tests,"
                                          "shell" "node" "test/resources/puppeteer.js" "test/resources" "run-tests.html"]
            "test-tests-advanced-es2015" ["do"
                                          "with-profile" "+testing-advanced,+es2015" "cljsbuild" "once" "tests,"
                                          "shell" "node" "test/resources/puppeteer.js" "test/resources" "run-tests-advanced.html"]
            "auto-test"                  ["do"
                                          "clean,"
                                          "with-profile" "+testing,+auto-testing" "cljsbuild" "auto" "tests"]
            "release"                    ["do"
                                          "shell" "scripts/check-versions.sh,"
                                          "clean,"
                                          "test,"
                                          "jar,"
                                          "shell" "scripts/check-release.sh,"
                                          "deploy" "clojars"]})
