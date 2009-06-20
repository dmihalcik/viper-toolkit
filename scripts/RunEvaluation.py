#! /usr/bin/env python

# RunEvaluation.py. Discuss at
# http://groups-beta.google.com/group/viper-toolkit
# 
# David Mihalcik davidm@cfar.umd.edu
# Adapted from RunEvaluation.pl
# by Scott Mennealy
import os, string

def getenv(envvar, default=None):
    if envvar in os.environ:
        return os.environ[envvar]
    else:
        return default

class ViperPE:
    def __init__(self):
        self.GTF = ""
        self.RDF = ""
        self.PR = ""
        self.EPF = ""
        self.NAME = ""
        self.TITLE = ""
        self.EVAL_RESULTS = getenv('EVAL_RESULTS', '')
        self.EVAL_EPF = getenv('EVAL_EPF', '')
        self.EVAL_GTF = getenv('EVAL_GTF', '')
        self.EVAL_HOME = getenv('EVAL_HOME', '')
        self.EVAL_PROP = getenv('EVAL_PROP', '')
        self.EVAL_RDF = getenv('EVAL_RDF', '')
        self.EVAL_RESULTS = getenv('EVAL_RESULTS', '')

        self.trans = {}
        self.noeval = 0
        self.template = None

    def makeEpf(self):
        input = f.open(template, 'r')
        tplt = input.read()
        input.close()

        for o, n in self.trans.items:
            string.replace(tplt, o, n)

        output = f.open(self.EVAL_EPF + os.sep + self.EPF, 'w')
        f.write(tplt)
        output.close()

    def replace(self, o, n):
        self.trans[o] = n

    def run(self):
        prFile = self.EVAL_PROP + os.sep + self.PR
        if self.template:
            if self.EPF == None or self.EPF == '':
                self.EPF = self.NAME + '.epf'
            self.makeEpf()
        epfFile = self.EVAL_EPF + os.sep + self.EPF
        gtfFile = self.EVAL_GTF + os.sep + self.GTF
        rdfFile = self.EVAL_RDF + os.sep + self.RDF
        runName = self.EVAL_RESULTS + os.sep + self.NAME + os.sep + self.NAME
        command = ["viper-pe", "-pr", prFile, "-epf", epfFile, "-g", gtfFile,
                   "-r", rdfFile, "-o", runName+".out",
                   "-raw", runName+".raw", "-l", runName+".log"]
        print "#"*70
        print "# Running evaluation: %s"%(' '.join(command))
        print "#"*70
        signal = os.spawnvp(os.P_WAIT, "viper-pe", command)
        if signal != 0:
            # Need to examine runevaluation.pl & viperpe.java for signals
            print "Recieved error signal from viper-pe: %d"%(signal)

    def graph(self):
        runDir = self.EVAL_RESULTS + os.sep + self.NAME 
        runName = runDir + os.sep + self.NAME
        configName = runName+".config"
        f = open(configName, 'w')
        f.write("1 %s\n%s.raw\n%s\n%s\n@date"%(runName,runName,runName,runName))
        f.close()
        curDir = os.getcwd()
        os.chdir(runDir)
        print "MAKING GRAPH"
        signal = os.spawnvp(os.P_WAIT, "makeGraph.pl", ["makeGraph.pl", configName])
        if signal != 0:
            print "Recieved error signal from makeGraph.pl: %d"%(signal)
        os.chdir(curDir)

class Graphs:
    def __init__(self):
        self.graphlists = []
    def include(self, v):
        self.graphlists.append([v])
    def append(self, v):
        if len(self.graphlists):
            self.graphlists[-1].append(v)
        else:
            self.graphlists = [[v]]
    def runCombinedGraphs(self, title):
        curDir = os.getcwd()
        resultDir = curDir + os.sep + title
        try:
            os.mkdir(resultDir)
        except OSError, x:
            if find(x, 'Errno 17'):
                pass # directory already exists
            else:
                raise
        os.chdir(resultDir)
        configName = title+'.config'
        f = open(configName, 'w')
        f.write("%d %s\n"%(len(self.graphlists), title))
        for subj in self.graphlists:
            l=[v.EVAL_RESULTS+os.sep+v.NAME+os.sep+v.NAME+'.raw' for v in subj]
            f.write("%s\n"%(', '.join(l)))
        for subj in self.graphlists:
            f.write("%s\n"%(subj[0].TITLE))
        f.write("%s\n@date"%(title))
        f.close()

        print "MAKING COMBINED GRAPH"
        signal = os.spawnvp(os.P_WAIT, "makeGraph.pl", ["makeGraph.pl", configName])
        if signal != 0:
            print "Recieved error signal from makeGraph.pl: %d"%(signal)
        os.chdir(curDir)

if __name__ == '__main__':
    #if 2 < len(sys.argv) < 5:
    #    print "USAGE: RunEvaluation.py [-noeval] <data_file> <template_file>";
    # A perl script could convert the data files into python scripts like this 
    # one, instead. This would have some benefits.
    G = Graphs()
    V = ViperPE()
    
    V.EVAL_HOME = '/fs/lamp/Projects/VACE/Evaluations/Text/DryRun'
    V.EVAL_DATA = V.EVAL_HOME + os.sep + 'DATA'
    V.EVAL_GTF  = V.EVAL_HOME + os.sep + 'GTF'
    V.EVAL_PROP = V.EVAL_HOME + os.sep + 'Properties'
    V.EVAL_RUNS = V.EVAL_HOME + os.sep + 'RUNS'
    
    V.EVAL_NAME = 'UMD'

    prefix = V.EVAL_RUNS + os.sep + V.EVAL_NAME + os.sep

    V.EVAL_RDF     = prefix + 'RDF'
    V.EVAL_RESULTS = prefix + 'Results'
    V.EVAL_EPF     = prefix + 'EPF'


    V.PR = 'textdetect.pr'
    V.GTF = 'all.gtf.xml'
    V.RDF = 'all.rdf.xml'
    V.NAME = 'frame-all'

    V.template = '/fs/lamp/Projects/VACE/Evaluations/Text/DryRun/RUNS/UMD/frame.template'
    V.replace('<filter>', '')
    V.replace('<frameMetrics>', 'matchedpixels missedpixels falsepixels fragmentation arearecall areaprecision [arearecall .7] [areaprecision .7]')

    V.run()
    V.graph()
