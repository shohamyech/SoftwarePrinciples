(ns clo-proj.core
  (:require [clojure.string :as str])
  (:require [clojure.java.io :as io])
  (:gen-class))

;"C:\Users\shoha\Desktop\SoftwarePrinciples\ex1\InputA.vm"

(println "Enter path")
(def path (read-line))

(when-not (.exists (java.io.File. path))
  (println "Invalid path")
  (System/exit 0))

(def output-file 
  (str path "\\" (re-find #"\w*$" path) ".asm"))

(def total-buy  0)
(def total-sell 0)

(defn HandleBuy [s]
  (let [cost (* (Double/parseDouble (get s 2))
                (Double/parseDouble (get s 3)))]
    (alter-var-root #'total-buy (constantly (+ total-buy cost)))

    (str "### BUY " (get s 1) " ###\n"
         (format "%,.2f" cost) "\n")))

(defn HandleSell [s]
  (let [cost (* (Double/parseDouble (get s 2))
                (Double/parseDouble (get s 3)))]
    (alter-var-root #'total-sell (constantly (+ total-buy cost)))

    (str "$$$ SELL " (get s 1) " $$$\n"
         (format "%,.2f" cost) "\n"))
)

(defn buy-or-sell [s]
  (if (= (first s) "buy")
    (HandleBuy s)
    (HandleSell s)))

(defn HandleFile [file]
  (apply str (first file) "\n"
         (->> (last file)
              (str/split-lines)
              (map #(str/split % #" "))
              (map buy-or-sell))))

(def files-names 
(->> (io/file path)
     (file-seq)
     (filter #(.isFile %))
     (map str)
     (map #(re-find #"\w*.vm$" %))
     (remove nil?)))

(->> (io/file path)
     (file-seq)
     (filter #(.isFile %))
     (map str)
     (filter #(re-find #".vm$" %))
     (map slurp)
     (map vector files-names)
     (map HandleFile)
     (str/join)
     (spit output-file))

(spit output-file
      (str "\nTOTAL BUY: " (format "%,.2f" total-buy)
           "\nTOTAL SELL: " (format "%,.2f" total-sell)) :append true)


