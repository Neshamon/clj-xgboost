(ns clj-boost-demo.core
  (:require [clj-boost.core :as boost]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]))

(def iris-path "resources/iris.csv")

(defn -main []
  (println "Hello World"))

(defn generate-iris
  [iris-path]
  (with-open [reader (io/reader iris-path)]
    (into []
          (comp (drop 1) (map #(split-at 4 %)))
          (csv/read-csv reader))))

(defn parse-float [s]
  (Float/parseFloat s))

(->> (generate-iris iris-path)
     (take 2)
     (map first)) ; thread-macro is super awesome!!!

(->> (generate-iris iris-path)
     (take 2)
     (map first)
     (map #(map parse-float %)))

(->> (generate-iris iris-path)
     (take 2)
     (map first)
     (map #(map parse-float %))
     (map vec))

(def transform-x
  (comp
   (map first)
   (map #(map parse-float %))
   (map vec)))

(def transform-y
  (comp
   (map last)
   (map (fn [label]
          (let [l (first label)]
                case l
                "setosa" 0
                "versicolor" 1
                "virginica" 2)))))

(defn munge-data [iris-data]
  (let [x (into [] transform-x iris-data)
        y (into [] transform-y iris-data)]
    (map conj x y))) ; Concatenates both lists
