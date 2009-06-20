

import os
import os.path
import re
import string
import sys
import xml.dom.minidom

letterRE = re.compile( r'\D+' )
digitRE = re.compile( r'\d+' )
viperNamespaceURI = 'http://lamp.cfar.umd.edu/viper'
viperdataNamespaceURI = 'http://lamp.cfar.umd.edu/viperdata'
shotIdRE = re.compile( r'^shot(\d+)_(\d+)$' )

def gcd( a, b ):
	if a == b:
		return a
	if b > a:
		(a, b) = (b, a)
	while( a % b ):
		(t, r) = divmod( a, b )
		(a, b) = (b, r)
	return b

def lcd( a, b ):
	d = gcd( a, b )
	return a * b / d

def isElement( node ):
	return node.nodeType == node.ELEMENT_NODE

class FormatException(Exception):
	def __init__(self, value):
		self.value = value
	def __str__(self):
		return `self.value`
class TimeFormatException(FormatException):
	def __init__(self, value):
		self.value = value
	def __str__(self):
		return `self.value`

class Time:
	def __init__(self):
		self.sec = 0
		self.numer = 0
		self.denom = 1000
	def __str__(self):
		return str( self.sec  +  float( self.numer ) / self.denom )
	def __add__(self, other):
		r = Time()
		if self.denom == other.denom:
			r.denom = self.denom
			r.numer = self.numer + other.numer
		else:
			d = gcd( self.denom, other.denom )
			selfFactor = other.denom / d
			otherFactor = self.denom / d
			r.denom = self.denom * selfFactor
			r.numer = (self.numer * selfFactor) + (other.numer * otherFactor)
		if r.numer > r.denom:
			(r.sec, r.numer) = divmod( r.numer, r.denom )
		r.sec = r.sec + self.sec + other.sec
		return r
	def inSeconds(self):
		return Time.__str__(self)
	def inMilliseconds(self):
		return str( int( self.sec * 1000 + float( self.numer ) / (float( self.denom ) / 1000 ) ) )

class MediaTimePoint(Time):
	"A class for interpreting ISO 8601 time points for MediaPoints Ds from MPEG-7"
	def __init__(self, str):
		Time.__init__(self)
		try:
			if str[:1] == 'T':
				S = str[1:]
				nums = string.split( S, ':' )
				self.sec = long( nums[0] ) * 3600 + long( nums[1] ) * 60 + long( nums[2] )
				(self.numer, self.denom) = map( long, string.split( nums[3], 'F' ) )
			else:
				raise TimeFormatException, 'Not a time point without days: ' + str
		except IndexError:
			raise TimeFormatException, 'Not a valid time point: ' + str
		except ValueError, ve:
			raise TimeFormatException, 'Not a valid time point: ' + str
	def __str__(self):
		tempSeconds = self.sec
		(hrs, tempSeconds) = divmod(tempSeconds, 3600)
		(mins, tempSeconds) = divmod(tempSeconds, 60)
		return 'T' + str(hrs) + ':' + str(mins) + ':' + str(tempSeconds) + ':' + str(self.numer) + 'F' + str(self.denom)

class MediaDuration(Time):
	"A class for interpreting subdiem ISO 8601 time spans for MediaDuration Ds from MPEG-7"
	def __init__(self, str):
		Time.__init__(self)
		if str[:2] == 'PT':
			S = str[2:]
			nums = letterRE.split(S)[:-1]
			postfixes = digitRE.split(S)[1:]
			if len(nums) and postfixes[0] == 'H':
				postfixes.pop(0)
				self.sec = self.sec + 3600 * int(nums.pop(0))
			if len(nums) and postfixes[0] == 'M':
				postfixes.pop(0)
				self.sec = self.sec + 60 * int(nums.pop(0))
			if len(nums) and postfixes[0] == 'S':
				postfixes.pop(0)
				self.sec = self.sec + int(nums.pop(0))
			if len(nums) and postfixes[0] == 'N':
				postfixes.pop(0)
				self.numer = int(nums.pop(0))
			if len(nums) and postfixes[0] == 'F':
				postfixes.pop(0)
				self.denom = int(nums.pop(0))
			if len(nums) or len(postfixes):
				raise TimeFormatException, "Error in MediaDuration format: " + str
		else:
			raise TimeFormatException, 'Not a duration in seconds: ' + str
	def __str__(self):
		sb = 'PT'
		tempSeconds = self.sec
		if tempSeconds > 3600:
			(hrs, tempSeconds) = divmod(tempSeconds, 3600)
			sb = sb + str(hrs) + 'H'
		if tempSeconds > 60:
			(mins, tempSeconds) = divmod(tempSeconds, 60)
			sb = sb + str(mins) + 'M'
		if tempSeconds > 0:
			sb = sb + str(tempSeconds)
		if numer > 0:
			sb = sb + str(self.numer) + 'N' + str(self.denom) + 'F'
		return sb

def getText(node):
	t = ''
	for n in node.childNodes:
		if n.nodeType == n.TEXT_NODE:
			t = t + n.data
	return t

def createDescConfig( root, name, type ):
	desc = root.createElement( 'descriptor' )
	desc.setAttribute( 'name', name )
	desc.setAttribute( 'type', type )
	return desc

def createAttrConfig( root, name, type ):
	attr = root.createElement( 'attribute' )
	attr.setAttribute( 'name', name )
	attr.setAttribute( 'type', type )
	return attr

def createSimpleValue( root, name, value, type ):
	attr = root.createElement( 'attribute' )
	attr.setAttribute( 'name', name )
	svalue = root.createElement( type )
	svalue.setAttribute( 'value', value )
	attr.appendChild( svalue )
	return attr

def createSvalue( root, name, value ):
	return createSimpleValue( root, name, value, 'data:svalue' )
def createDvalue( root, name, value ):
	return createSimpleValue( root, name, str( value ), 'data:dvalue' )
def createFvalue( root, name, value ):
	return createSimpleValue( root, name, str( value ), 'data:fvalue' )

def _getMediaTime(el):
	shotStart = MediaTimePoint( getText( el.getElementsByTagName( 'MediaTimePoint' )[0] ) )
	shotDuration = MediaDuration( getText( el.getElementsByTagName( 'MediaDuration' )[0] ) )
	return (shotStart, shotStart + shotDuration)
def _getMediaTimeConfig(doc):
	return None

def _getTextAnnotation(el):
	confidence = float( el.getAttributeNS( None, 'confidence' ) )
	str = getText( el.getElementsByTagName( 'FreeTextAnnotation' )[0] )
	return (confidence, str)
def _getTextAnnotationConfig(doc):
	desc = createDescConfig( doc, 'TextAnnotation', 'CONTENT' )
	desc.appendChild( createAttrConfig( doc, 'confidence', 'fvalue' ) )
	desc.appendChild( createAttrConfig( doc, 'value', 'svalue' ) )
	return desc
def _getTextAnnotationElement(doc, val):
	(confValue, textValue) = val
	content = doc.createElement( 'content' )
	content.setAttribute( 'name', 'TextAnnotation' )
	content.appendChild( createFvalue( doc, 'confidence', confValue ) )
	content.appendChild( createSvalue( doc, 'value', str( textValue ) ) )
	return content


BINARY_FEATURES = ['IsCityscape', 'HasFace',  'IsIndoors', 'IsInstrumental',
	 'IsLandscape', 'IsMonologue', 'IsOutdoors',
	'HasPeople', 'IsSpeech', 'HasTextOverlay' ]

BINARY_FEATURE_MAP = {'cityscape':'IsCityscape', 
	'face':'HasFace', 
	'indoors':'IsIndoors',
	'instrumental':'IsInstrumental',
	'landscape':'IsLandscape',
	'monologue':'IsMonologue',
	'outdoors':'IsOutdoors',
	'people':'HasPeople',
	'speech':'IsSpeech',
	'textoverlay':'HasTextOverlay',

	'instrumental sound':'IsInstrumental',
	'have text':'HasTextOverlay'
	}
def _getBinaryFeature(el):
	confidence = float( el.getAttributeNS( None, 'confidence' ) )
	relevance = float( el.getAttributeNS( None, 'relevance' ) )
	name = string.lower( string.strip( getText( el.getElementsByTagName( 'Keyword' )[0] ) ) )
	if name in BINARY_FEATURE_MAP:
		name = BINARY_FEATURE_MAP[name]
	else:
		raise FormatException, 'Not a recognized keyword: \'' + name + '\''
	return (name, confidence, relevance)
def _getBinaryFeatureConfig(doc, name):
	desc = createDescConfig( doc, name, 'CONTENT' )
	desc.appendChild( createAttrConfig( doc, 'confidence', 'fvalue' ) )
	desc.appendChild( createAttrConfig( doc, 'relevance', 'fvalue' ) )
	return desc
def _getBinaryFeatureElement(doc, val):
	(name, confidence, relevance) = val
	content = doc.createElement( 'content' )
	content.setAttribute( 'name', name )
	content.appendChild( createFvalue( doc, 'confidence', confidence ) )
	content.appendChild( createFvalue( doc, 'relevance', relevance ) )
	return content

def _getKeyFrameConfig(doc):
	desc = createDescConfig( doc, 'KeyFrame', 'CONTENT' )
	desc.appendChild( createAttrConfig( doc, 'filename', 'svalue' ) )
	desc.appendChild( createAttrConfig( doc, 'framenum', 'dvalue' ) )
	return desc
def _getKeyFrameElement(doc, val):
	(filename, framenum) = val
	content = doc.createElement( 'content' )
	content.setAttribute( 'name', 'KeyFrame' )
	content.appendChild( createSvalue( doc, 'filename', str( filename ) ) )
	content.appendChild( createDvalue( doc, 'framenum', framenum ) )
	return content

class ShotData:
	"Contains information about a single shot"
	_parsers = {'MediaTime': _getMediaTime, 'TextAnnotation': _getTextAnnotation}
	_printers = {'TextAnnotation': _getTextAnnotationElement, 'KeyFrame': _getKeyFrameElement}
	def __init__(self):
		self.attr = {}
	def addData(self, el):
		lname = el.localName
		if lname == 'MediaTime':
			self.attr[lname] = apply( ShotData._parsers[lname], [el] )
		elif lname == 'TextAnnotation':
			keywords = el.getElementsByTagName( 'KeywordAnnotation' )
			if len( keywords ) > 0:
				val = _getBinaryFeature( el );
				self.attr[val[0]] = val
			else:
				self.attr[lname] = apply( ShotData._parsers[lname], [el] )
		else:
			raise FormatException, 'Not a recognized mpeg data type: ' + lname
	def setKeyFrame(self, filename, framenum):
		self.attr['KeyFrame'] = (filename, framenum)
	def getConfig(self, doc):
		config = doc.createElement( 'config' )
		config.appendChild( _getTextAnnotationConfig( doc ) )
		config.appendChild( _getKeyFrameConfig( doc ) )
		for bf in BINARY_FEATURES:
			config.appendChild( _getBinaryFeatureConfig( doc, bf ) )
		return config
	def getElements(self, doc):
		r = []
		if 'MediaTime' in self.attr:
			timespan = self.attr['MediaTime']
			tspan = timespan[0].inMilliseconds() + ':' + timespan[1].inMilliseconds()
			for (type, val) in self.attr.iteritems():
				if type in ShotData._printers:
					content = ShotData._printers[type]( doc, val )
					content.setAttribute( 'timespan', tspan )
					r = r + [content]
				elif type != 'MediaTime':
					try:
						content = _getBinaryFeatureElement( doc, val )
						content.setAttribute( 'timespan', tspan )
						r = r + [content]
					except ValueError, ve:
						print 'Error in binary feature ' + type + ', ' + str( val ) + ': ' + str( ve )
		return r
	def __str__(self):
		return str( self.attr )


class FileInformation:
	"Contains information about a file"
	def __init__(self):
		self.attr = {}
	def setInformation(self, el):
		for node in el.childNodes:
			if node.nodeType == node.ELEMENT_NODE:
				if node.nodeName == 'duration':
					duration = int( node.getAttributeNS( None, 'second' ) )
					duration += 60 * int( node.getAttributeNS( None, 'minute' ) )
					duration += 3600 * int( node.getAttributeNS( None, 'hour' ) )
					self.attr['duration'] = duration
				else:
					self.attr[node.nodeName] = getText( node )
	def setThumbnail(self, filename):
		self.attr['thumb'] = filename
	def getConfig(self, doc):
		desc = doc.createElement( 'descriptor' )
		desc.setAttribute( 'type', 'FILE' )
		desc.setAttribute( 'name', 'Information' )
		desc.appendChild( createAttrConfig( doc, 'SOURCEFILE', 'svalue' ) )
		desc.appendChild( createAttrConfig( doc, 'TITLE', 'svalue' ) )
		desc.appendChild( createAttrConfig( doc, 'FILESIZE', 'dvalue' ) )
		desc.appendChild( createAttrConfig( doc, 'MD5', 'svalue' ) )
		desc.appendChild( createAttrConfig( doc, 'SOURCE', 'svalue' ) )
		desc.appendChild( createAttrConfig( doc, 'YEAR', 'dvalue' ) )
		desc.appendChild( createAttrConfig( doc, 'DURATION', 'dvalue' ) )
		desc.appendChild( createAttrConfig( doc, 'THUMB', 'svalue' ) )
		desc.appendChild( createAttrConfig( doc, 'MIMETYPE', 'svalue' ) )
		desc.appendChild( createAttrConfig( doc, 'FRAMERATE', 'fvalue' ) ) # frames per second
		return desc
	def getElement(self, doc):
		el = doc.createElement( 'file' )
		el.setAttribute( 'name', 'Information' )
		if 'filename' in self.attr:
			el.appendChild( createSvalue( doc, 'SOURCEFILE', self.attr['filename'] ) )
		if 'title' in self.attr:
			el.appendChild( createSvalue( doc, 'TITLE', self.attr['title'] ) )
		if 'filesize' in self.attr:
			el.appendChild( createDvalue( doc, 'FILESIZE', self.attr['filesize'] ) )
		if 'md5sum' in self.attr:
			el.appendChild( createSvalue( doc, 'MD5', self.attr['md5sum'] ) )
		if 'source' in self.attr:
			el.appendChild( createSvalue( doc, 'SOURCE', self.attr['source'] ) )
		if 'year' in self.attr:
			el.appendChild( createDvalue( doc, 'YEAR', self.attr['year'] ) )
		if 'duration' in self.attr:
			el.appendChild( createDvalue( doc, 'DURATION', self.attr['duration'] ) )
		if 'thumb' in self.attr:
	                el.appendChild( createSvalue( doc, 'THUMB', self.attr['thumb'] ) )
		el.appendChild( createSvalue( doc, 'MIMETYPE', 'video/mpeg' ) )
		el.appendChild( createFvalue( doc, 'FRAMERATE', '29.97' ) )
		return el
	def getFilename(self):
		if 'filename' in self.attr:
			return self.attr['filename']
		else:
			return None

class ShotDatabase:
	"Contains information about the shots in the TREC video database"
	def __init__(self):
		self.db = {}
		self.fileInfo = {}
	def addShots(self, shotfile):
		"Adds a shotfile, which describes a file, to the ShotData db"
		root = xml.dom.minidom.parse( shotfile )
		for video in root.getElementsByTagName( 'Video' ):
			vidId = int( video.getAttributeNS( None, 'id' ) )
			if vidId in self.db:
				forThis = self.db[vidId]
			else:
				forThis = []
				self.db[vidId] = forThis
			timeNode = video.getElementsByTagName( 'TemporalDecomposition' )[0]
			for shot in timeNode.getElementsByTagName( 'VideoSegment' ):
				shotId = shot.getAttributeNS( None, 'id' )
				match = shotIdRE.match( shotId )
				if match == None:
					raise FormatException, 'Not a valid shotid: ' + shotId
				(vNum, sNum) = map( int, match.groups() )
				if vNum != vidId:
					raise FormatException, 'Not a valid shotid for vid #' + vidId + ': ' + shotId
				if sNum >= len( forThis ):
					forThis[len(forThis):sNum] = [ShotData() for i in xrange(len(forThis), sNum + 1)]
				#print 'adding: ' + str( sNum )
				for el in filter( isElement, shot.childNodes ):
					try:
						forThis[sNum].addData( el )
					except FormatException, fe:
						print sys.stderr.write( 'Error in ' + shotfile + ': ' + fe.value + os.linesep)
	def addKeyFrames(self, rootDir, lockDir='/fs/lamp/Databases/video/TREC2002/keyframes'):
		"Looks through a keyframe directory for all paths in the form <rootDir>/<vidId>/l/<framenum>.jpg"
		if lockDir == None:
			lockDir = rootDir
		for vidDir in os.listdir( rootDir ):
			lcname = lockDir + '/' + vidDir + '/l'
			fname = rootDir + os.sep + vidDir
			childname = fname + os.sep + 'l'
			if os.path.isdir( fname ) and os.path.isdir( childname ):
				vidId = int( vidDir )
				if vidId in self.db:
					v = self.db[vidId]
					keyframes = map( lambda str: int( str[:-4] ), os.listdir( childname ) )
					keyframes.sort()
					for datum in v[1:]:
						kf = keyframes.pop( 0 )
						datum.setKeyFrame( lcname + '/' + str( kf ) + '.jpg', kf )
				self.fileInfo[vidId].setThumbnail( v[1].attr['KeyFrame'][0] )
	def addCollectionInformation(self, collectionfile):
		"Adds a videoId -> filename map, and FILE Information descriptor information"
		root = xml.dom.minidom.parse( collectionfile )
		for videoFile in root.getElementsByTagName( 'VideoFile' ):
			vidId = int( getText( videoFile.getElementsByTagName( 'id' )[0] ) )
			if vidId in self.fileInfo:
				forThis = self.fileInfo[vidId]
			else:
				forThis = FileInformation()
				self.fileInfo[vidId] = forThis
			forThis.setInformation( videoFile )
	def getGtDom(self):
		domI = xml.dom.minidom.getDOMImplementation()
		dtd = domI.createDocumentType( 'viper', 'viper', viperNamespaceURI )
		doc = domI.createDocument( viperNamespaceURI, 'viper', dtd )
		viper = doc.createElement( 'viper' )
		viper.setAttribute( 'xmlns', viperNamespaceURI );
		viper.setAttribute( 'xmlns:data', viperdataNamespaceURI );
		if len( self.db ):
			config = self.db.values()[0][0].getConfig( doc )
			if len( self.fileInfo ):
				config.appendChild( self.fileInfo.values()[0].getConfig( doc ) )
			viper.appendChild( config )
			data = doc.createElement( 'data' )
			for (fileid, values) in self.db.iteritems():
				sourcefile = doc.createElement( 'sourcefile' )
				if fileid in self.fileInfo:
					info = self.fileInfo[fileid]
					filename = info.getFilename()
					sourcefile.appendChild( info.getElement( doc ) )
				else:
					filename = None
				if None == filename:
					sourcefile.setAttribute( 'filename', str( fileid ) )
				else:
					sourcefile.setAttribute( 'filename', filename )
				for datum in values:
					for el in datum.getElements( doc ):
						sourcefile.appendChild( el )
				data.appendChild( sourcefile )
			viper.appendChild( data )
		return viper
	def __str__(self):
		return self.getGtDom().toxml()

q = ShotDatabase()
q.addCollectionInformation( r'H:\Databases\video\TREC2002\shotboundaries\collection.xml' )
for d in sys.argv[1:]:
	if os.path.isdir( d ):
		for f in filter( lambda x: (x[-4:]=='.xml' and len( x ) < 10), os.listdir( d ) ):
			q.addShots( d + os.sep + f )
	else:
		q.addShots( d )
q.addKeyFrames( r'H:\Databases\video\TREC2002\keyframes' )
print q
