# CIT-BACH

## Software description
CIT-BACH (pronounced as "sit back" or "sea eye tea back") is a test case generation tool for combinatorial interaction testing. CIT-BACH is written completely in Java and requires JRE 1.6.0 or higher to run.

CIT-BACH uses a one-row-at-a-time greedy algorithm, which is similar to PICT. The tool supports constraints. To handle constraints, the tool first builds a binary decision diagram (BDD) that represents all the constraints. The way of doing this is novel in that once the BDD has been created, there is no need to manipulate it: Whether a test case or tuple satisfies the constraints or not can be checked by simply traversing the BDD from the root to the leaf, which only requires time linear in the number of parameters.

## Licence
CIT-BACH is free software under the zlib license. CIT-BACH uses JDD for BDD manipulation. JDD is also free software under the zlib license.

## Usage
Here is an example of an input file which describes the System Under Test (SUT).

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

To obtain a test suite that satisfies pair-wise coverage, run the tool as follows:

```
java -jar cit-bach.jar -i inputfile -lang en
```
Below is the output.

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

## Options
- -c [2 | 3 | 4 | 5 | all] : Strength (default: 2)
- -s file: Seeding file
- -i file: Model file
- -o file: Output file
- -random N: Random seed
- -repeat N: N times repetition
- -lang [JP | EN] : Language
- -policy: License notice
