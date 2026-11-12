[English](README.md) | 日本語

# CIT-BACH

## ソフトウェアの概要
CIT-BACH（「シットバック」または「シーアイティーバック」と読みます）は、組合せテストのためのテストケース生成ツールです。CIT-BACH はすべて Java で記述されており、実行には Java 17 以上が必要です。現在の開発版はバージョン 1.2 で、引き続き開発中です。

CIT-BACH は PICT と同様の、1 行ずつテストケースを生成する貪欲アルゴリズム（one-row-at-a-time greedy algorithm）を用いています。本ツールは制約をサポートしています。制約を扱うために、本ツールはまずすべての制約を表現する二分決定図（BDD）を構築します。この手法の新規性は、一度 BDD を構築した後は BDD を操作する必要がない点にあります。テストケースやタプルが制約を満たすかどうかは、BDD を根から葉まで辿るだけで判定でき、その計算時間はパラメータ数に対して線形です。

## ライセンス
CIT-BACH は MIT ライセンスのもとで提供されるフリーソフトウェアです。CIT-BACH は BDD の操作に JDD を使用しています。JDD は zlib ライセンスのソフトウェアです。
また、入力ファイルの文字コードを判定するために ICU4J を使用しています。

## 使い方
以下は、テスト対象システム（SUT）を記述した入力ファイルの例です。

```
# parameters and values
Display (16MC 8MC BW)
Viewer (Graphical Text None)
Camera (2MP 1MP None)
VideoCamera (Yes No)
VideoRingtone  (Yes No)

# group: ensures all value combinations are covered 
{Display Viewer Camera}

# constraints
#1
(if (== [Viewer] Graphical) (or (== [Display] 16MC) (== [Display] 8MC)))

#2
(if (== [Camera] 2MP) (or (== [Display] 16MC) (== [Display] 8MC)))

#3
(if (== [Viewer] Graphical) (not (== [Camera] 2MP)))

#4
(if (== [Display] 8MC) (<> [Camera] 2MP))

#5
(if (== [VideoCamera] Yes) 
   (and (or (== [Camera] 2MP) (== [Camera] 1MP))  
        (or (== [Display] 16MC) (== [Display] 8MC))
   )
)

#6
(if (== [VideoRingtone] Yes) (== [VideoCamera] Yes))

#7
(not (and (== [Display] 16MC) (== [Viewer] Text) (== [Camera] 2MP)))
```

（`#` で始まる行はコメントです。上から順に、パラメータと値の定義、グループ（指定したパラメータ間の値の組合せをすべて網羅することを保証します）、制約を記述しています。）

2 因子間網羅（ペアワイズ）を満たすテストスイートを得るには、以下のようにツールを実行します。

```
java -jar cit-bach.jar -i inputfile
```
出力は以下のとおりです。

```
#SUCCESS,4358,i,Model1.txt,s,,o,,c,2,random,4358,repeat,1
Display,Viewer,Camera,VideoCamera,VideoRingtone
16MC,None,2MP,Yes,Yes
16MC,Graphical,1MP,No,No
8MC,Graphical,1MP,Yes,Yes
16MC,Text,1MP,Yes,No
8MC,Text,1MP,No,No
BW,Text,1MP,No,No
16MC,None,1MP,No,No
8MC,None,1MP,No,No
BW,None,1MP,No,No
16MC,Graphical,None,No,No
8MC,Graphical,None,No,No
16MC,Text,None,No,No
8MC,Text,None,No,No
BW,Text,None,No,No
16MC,None,None,No,No
8MC,None,None,No,No
BW,None,None,No,No
16MC,None,2MP,No,No
16MC,Text,1MP,Yes,Yes
```

## オプション
- -c [2 | 3 | 4 | 5 | all] : 網羅の強度（デフォルト: 2）
- -s file: シードファイル
- -i file: モデルファイル
- -o file: 出力ファイル
- -random N: 乱数のシード
- -repeat N: N 回の繰り返し
- -lang [JP | EN] : 言語
- -policy: ライセンス表示

## 引用
CIT-BACH で用いている制約処理アルゴリズムは、以下の論文で解説しています。

```
@INPROCEEDINGS{Tsuchiya2024,
  author={Tsuchiya, Tatsuhiro},
  booktitle={2024 IEEE International Conference on Software Testing, Verification and Validation Workshops (ICSTW)}, 
  title={Use of binary decision diagrams to handle constraints in combinatorial interaction testing}, 
  year={2024},
  volume={},
  number={},
  pages={211-214},
  keywords={Software testing;Constraint handling;Boolean functions;Conferences;Binary decision diagrams;Software;software testing;combinatorial testing;constraint;binary decision diagram},
  doi={10.1109/ICSTW60967.2024.00048}}
```
