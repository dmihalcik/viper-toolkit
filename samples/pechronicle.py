#! /usr/bin/env jython

import os, string, sys, time, re
import java
from edu.umd.cfar.lamp.viper.chronology import *
from edu.umd.cfar.lamp.viper.core import *

def splitfiles(flist):
    return splitfiles.pat.split(flist)
splitfiles.pat = re.compile('[, ]+')

def section(fo):
    "divides a raw file into named sections. assumes no repetition of titles"
    sections = {}
    lines = fo.readlines()
    state = 0
    start = 0
    title = ''
    curr = 0
    for line in lines:
        if state == 0:
            if line.startswith('#BEGIN_'):
                title = line[7:].strip()
                state = 1
                start = curr + 1
        elif state == 1:
            if line.startswith('#END_'):
                sections[title] = lines[start:curr]
                state = 0
        curr = curr + 1
    if state == 1:
        sections[title] = lines[start:]
    return sections

def trim(s):
    return s.strip()

def parsemetrics(lines):
    "Gets the objects and attributes that are evaluated"
    metrics = {}
    currattrs = []
    name = ''
    lines = filter(len, map(trim, lines))
    for l in lines:
        if l.startswith('*'):
            currattrs.append(string.split(l)[1])
        else:
            currattrs = []
            metrics[string.split(l)[0]] = currattrs
    return metrics


def splitfiles(flist):
    return map(trim, splitfiles.pat.split(flist))
splitfiles.pat = re.compile('[, ]+')

def candstrip(s):
    "gets a pair of lists, ids and distances"
    s = s.strip()
    ids = []
    distances = []
    if s.startswith('['):
        i = s.find(']')
        ids = map(int, splitfiles(s[1:i]))
    else:
        i = s.find(' ')
        ids = [int(s[:i])]
    distances = map(float, string.split(s[i+1:]))
    return (ids, distances)

class ObjectLine:
    def __init__(self, line):
        m = ObjectLine.pat.match(line)
        self.valid = 0
        if m:
            self.valid = 1
            self.name, self.id, self.quality, self.level, rest = m.groups()
            if self.id.startswith('['):
                self.id = filter(int, splitfiles(self.id[1:-1]))
            else:
                self.id = [int(self.id)]
            self.level = int(self.level)
            if self.quality == 'DETECT':
                m = ObjectLine.rest.match(rest)
                self.distance = float(m.group(1))
                if self.distance < 0:
                    self.distance = 0
                # matches = int(m.group(2)) == len(rest)
                all = ObjectLine.cand.findall(m.group(3))
                self.matches = map(candstrip, all)
ObjectLine.pat = re.compile('^(\w+) ((?:\d+)|(?:\[[^\]]+\])) (\w+) (\d+)(.*)$')
ObjectLine.rest = re.compile(' (\S+) (\d+) (.*)$')
ObjectLine.cand = re.compile('^\s*\[((?:(?:\d+)|(?:\[[^\]]+\]))[^\]]+)\]')

def getmeasures(goodtargets, missed, goodcandidates, false):
    precision, recall, f1 = (0,0,0)
    if getmeasures.GRAPH_TYPE == "Target":
        if goodtargets + false > 0:
            precision = float(goodtargets) / (goodtargets+false) * 100
    elif getmeasures.GRAPH_TYPE == "Balanced":
        if goodcandidates + false > 0:
            precision = float(goodcandidates) / (goodcandidates+false) * 100
    if goodtargets + missed > 0:
        recall = float(goodtargets) / (goodtargets+missed) * 100
    if precision + recall > 0:
        f1 = (2. * precision * recall) / (precision + recall)
    return (precision, recall, f1)
getmeasures.GRAPH_TYPE = "Balanced"

class RawCompare:
    def __init__(self, config):
        f = open (sys.argv[1],'r')
        l = string.split(f.readline())
        numRun = int(l[0])
        self.outputName = l[1]

        lines = f.readlines()
        self.runFiles = map(splitfiles, lines[:numRun])
        self.legend = lines[numRun:numRun+numRun]
        self.subtitle = string.join(lines[numRun+numRun:], '\n')

        f.close()

        self.typemap = {'Standard':RawCompare.standard, 'Pixel':RawCompare.framewise, 'Tracking':RawCompare.tracking}

    def checkType(self):
        f = open(self.runFiles[0][0],'r')
        for l in f.readlines():
            if l.startswith('#BEGIN_RESULTS'):
                self.type = 'Standard'
            if l.startswith('#BEGIN_PIXEL_RESULTS'):
                self.type = 'Pixel'
            if l.startswith('#BEGIN_TRACKING_RESULTS'):
                self.type = 'Tracking'
        f.close()

    def expand(self):
        string.replace(self.subtitle, '@date', time.strftime('%c'))

    def run(self, size=(640,480), type='png'):
        self.expand()
        self.checkType()
        charts = apply(self.typemap[self.type], (self,))

        type = type.lower()
        for name, ch in charts.items():
            of = java.io.File('%s_%s.%s'%(self.outputName, name,type))
            if type == 'png':
                ChartUtilities.saveChartAsPNG(of, ch, size[0], size[1])
            elif type == 'svg':
                domImpl = DOMImplementationImpl.getDOMImplementation()
                document = domImpl.createDocument(null, "svg", null);

                # Create an SVG Context to customise
                ctx = SVGGeneratorContext.createDefault(document);
                ctx.setComment("JFreeChart generated with makeGraph.py");

                # Create an instance of the SVG Generator
                svgGenerator = new SVGGraphics2D(ctx, false);
                svgGenerator.setSVGCanvasSize(new Dimension(width,height));
                chart.draw(svgGenerator, new Rectangle2D.Double(0, 0, width, height), null);

                writer = new FileWriter (of)
                svgGenerator.stream(writer, false);
                writer.close()
            elif type == 'jpeg' or type == 'jpg':
                ChartUtilities.saveChartAsJPEG(of, ch, size[0], size[1])

    def standard(self):
        charts = {}
        countdata = []
        percentdata = []
        f1data = []
        dists = []
        distanceMax = 1
        for fs in self.runFiles:
            count = {'files': len(fs), 'missed': 0, 'false':0,
                     'goodtargets':0, 'goodcandidates':0, 'distance':0}
            distances = []
            for f in fs:
                print 'Reading file:', f
                fo = open(f,'r')
                s = section(fo)
                fo.close()

                print '  Parsing Metrics'
                attributes = parsemetrics(s['METRICS'])

                print '  Parsing Results'
                for line in s['RESULTS']:
                    ol = ObjectLine(line)
                    if ol.valid:
                        count['distance'] += 1
                        if 'FALSE' == ol.quality:
                            count['false'] += 1
                        elif 'MISSED' == ol.quality:
                            count['missed'] += 1
                        elif 'DETECT' == ol.quality:
                            count['goodtargets'] += len(ol.id)
                            count['goodcandidates'] += len(ol.matches[0][0])
                            distances.append (ol.distance)
            # include counts
            countdata.append([count['goodtargets'], count['missed'], count['false']])

            # include percents
            targetcount = float(count['goodtargets'] + count['missed'])
            if targetcount:
                percentdata.append([count['goodtargets'] / targetcount * 100,
                                    count['missed'] / targetcount * 100])
            else:
                percentdata.append([0,100])

            # include f1 data
            f1data.append(getmeasures(count['goodtargets'], count['missed'],
                                      count['goodcandidates'], count['false']))

            # include distances
            distances.sort()
            dists.append(distances)

        # Making Count Graph
        ds = DefaultCategoryDataset (countdata)
        ds.seriesNames = self.legend
        ds.categories = ['DETECTED', 'MISSED', 'FALSE']
        name = 'Error/Detection Counts\n' + self.subtitle
        ch = ChartFactory.createVerticalBarChart(name, 'Results', 'Number', ds, 1)
        charts['Error1'] = ch

        # Making count/percent graph

        # Making

        return charts
    def framewise(self):
        pass
    def tracking(self):
        pass

class ComparisonChronicleSegmentMaster(chronology.PTimeSegmentFactoryFactory):
	"Class for mastering segment manufacturers"

class ComparisonChronicleModel(chronology.ChronicleModel):
	

class CompareEvaluations:
	def __init__(self):
		self.raws = {}
		self.targets = []
		self.candidates = {}

	def addRaw(self, raw):
		name = raw.filename
		self.raws[name] = raw
		self.candidates[name] = api.impl.ViperParser.parseFromTextFile(raw.candidates)
		if (self.targets == [])
			self.targets = api.impl.ViperParser.parseFromTextFile(raw.targets)

	def getTruth(self):
		return self.targets
	
	def getSourceName(self):
		return self.targets.getAllSourcefiles().get(0).getSourcefileName()

class WindowExits(java.awt.event.WindowAdapter):
	def windowClosing(self,event):
		sys.exit(0)

if __name__ == '__main__':
	comparison = CompareEvaluations ()
	for f in sys.argv[1:]:
		comparison.addRaw(Raw(f))
	data = comparison.getTruth()
	mediator = core.ViperViewMediator(data, comparison.getSourceName())
	chronoView = ComparisonChronicleView(comparison, mediator)
	chronoView.expandAll(chronoView.getRootTimeLineView(), 0);
	container = swing.JFrame("Viper Data View")
	container.addWindowListener(WindowExits())
	container.setSize(chronoView.getPreferredSize())
	container.getContentPane().add(chronoView)
	container.validate()
	chronoView.requestFocus()
	container.setVisible(1)