/***************************************
 *            ViPER                    *
 *  The Video Processing               *
 *         Evaluation Resource         *
 *                                     *
 *  Distributed under the GPL license  *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package viper.comparison;


import java.io.*;
import java.util.*;

import viper.comparison.distances.*;
import viper.descriptors.*;
import viper.descriptors.attributes.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cfar.lamp.viper.util.reader.*;

/**
 * The standard evaluation that counts the number 
 * of matching descriptors.
 */
public class ObjectEvaluation implements Evaluation
{
    private void helpPrintMetricsTo (Descriptor D, Map<String,AttrMeasure> M, PrintWriter output)
    {
        output.print ("\n" + D.getCategory() + " " + D.getName());
        AttrMeasure fspanM = M.get (" framespan");
        if (fspanM != null) {
            output.print ("\t[" + fspanM + "]");
        }
        output.println ();
        for (Map.Entry<String,AttrMeasure> curr :  M.entrySet()) {
            String attrib = (String) curr.getKey();
            if (!attrib.equals (" framespan")) {
                AttrMeasure meas = (AttrMeasure) curr.getValue();
                output.println ("\t" + attrib + "\t [" + meas + "]");
            }
        }
    }
    
    /**
     * @inheritDoc
     */
    public void printMetricsTo (PrintWriter output)
    {
        for (Map.Entry<DescPrototype, Map<String, AttrMeasure>> curr : descriptors.entrySet()) {
            helpPrintMetricsTo ((Descriptor) curr.getKey(),
                                (Map<String, AttrMeasure>) curr.getValue(), output);
        }
    }

    /**
     * @inheritDoc
     */
    public final void printRawMetricsTo( PrintWriter raw )
    {
        for (Iterator iter = descriptors.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry curr = (Map.Entry) iter.next();
            helpPrintRawMetricsTo ((Descriptor) curr.getKey(),
                                   (Map) curr.getValue(), raw);
        }
    }
    private void helpPrintRawMetricsTo (Descriptor D, Map M, PrintWriter raw)
    {
        raw.print(D.getName());
        AttrMeasure fspanM = (AttrMeasure) M.get (" framespan");
        if (fspanM != null) {
            raw.print (" " + fspanM);
        }
        raw.println();
        for (Iterator iter = M.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry curr = (Map.Entry) iter.next();
            String attrib = (String) curr.getKey();
            if (!attrib.equals (" framespan")) {
                AttrMeasure meas = (AttrMeasure) curr.getValue();
                raw.println ("   * " + attrib + " " + meas);
            }
        }
    }
    
    /**
     * @see Evaluation#parseEvaluation(VReader, DescriptorConfigs)
     */
    public void parseEvaluation(VReader reader, DescriptorConfigs dcfgs) throws IOException
    {
        //  Parse Each line in the EVALUATION section
        //  For each descriptor, we get the descriptor element (eg OBJECT TEXT)
        //  Set it to true if it is found
        while (!reader.currentLineIsEndDirective()) {
            CountingStringTokenizer st 
                    = new CountingStringTokenizer (reader.getCurrentLine());
            // The category of descriptor (ie FILE, OBJECT, etc.)
            String type = st.nextToken();
            if (!Descriptor.isCategory (type)) {
                reader.printError (type + " is not a Descriptor category.",
                                   st.getStart(), st.getEnd());
                reader.gotoNextRealLine();
            } else {
                int startDescCol = st.getStart();
                String name = st.nextToken();
                int endDescCol = st.getEnd();
                // The descriptor that this line refers to.
                Iterator relevantIter = dcfgs.getNodesByType (type, name);
                if (!relevantIter.hasNext()) {
                    reader.printError ("Not a Descriptor type specified " 
                                       + "in the Configuration " + endDescCol,
                                       startDescCol, endDescCol);
                    reader.gotoNextRealLine();
                } else {
                    DescPrototype relevant = (DescPrototype) relevantIter.next();
                    descriptors.put (relevant, helpParseAttribMap(reader, relevant));
                }
            }
        }
    }
    
    private Map<String, AttrMeasure> helpParseAttribMap(VReader reader, DescPrototype proto) 
        throws IOException, NoSuchElementException
    {
        TreeMap<String, AttrMeasure> attribMap = new TreeMap<String, AttrMeasure>();

        StringTokenizer st = new StringTokenizer (reader.getCurrentLine());

        try {
            st.nextToken(); st.nextToken(); // skip name
            attribMap.put (" framespan", new AttrMeasure (" framespan", st));

        } catch (ImproperMetricException imx) {
            reader.printError (imx.getMessage ());
        } catch (NoSuchElementException nsex ) {
            reader.printWarning( "Evaluation not properly treating metric" );
        }
        
        reader.gotoNextRealLine();
        
        while (!Descriptor.startsWithCategory( reader.getCurrentLine() )
	       && !reader.currentLineIsEndDirective()) {
            st = new StringTokenizer (reader.getCurrentLine());
            String attribName = st.nextToken();
            AttributePrototype curr = proto.getAttributePrototype(attribName);
            if (st.hasMoreTokens()) {
                try {
                    if (!st.nextToken().equals (":")) {
                        reader.printError( "Improper placement of colon" );
                    } else {
                        attribMap.put (attribName, new AttrMeasure (curr.getType(), st));
                        if (st.hasMoreTokens()) {
                            throw (new BadDataException ("Unparsed data at end of line"));
                        }
                    }
                } catch (BadDataException bdx) {
                    reader.printWarning (bdx.getMessage());
                } catch( ImproperMetricException imx ) {
                    reader.printWarning( imx.getMessage() );
                }
            } else {
                attribMap.put (attribName, new AttrMeasure (curr.getType()));
            }
            reader.gotoNextRealLine();
        }
        return attribMap;
    }

    /**
     * This map describes which attributes to evaluate and what measures to use
     * on them. It is of the form (Descriptor)->(AttribMap), where AttribMap
     * is of the form (String AttribName)->(AttrMeasure)
     * Note that AttribName is " framespan" for the framespan measure.
     */
    private TreeMap<DescPrototype, Map<String, AttrMeasure>> descriptors = new TreeMap<DescPrototype, Map<String, AttrMeasure>>(EvaluationParameters.descriptorComparator);

    private EvaluationParameters epf;
    
    /** 
     * Creates a new instance of ObjectEvaluation 
     * @param epf the evaluation paramters
     * @param level the level to bring the evaluation to
     * @param targetMatch the target match filter type, eg MULTIPLE
     */
    public ObjectEvaluation (EvaluationParameters epf, int level, int targetMatch) {
        this.epf = epf;
        this.level = level;
        this.targetMatch = targetMatch;
    }
    
    
    
    /**
     * @inheritDoc
     */
    public PrintWriter getOutput() {
        return output;
    }
    
    /**
     * Get an estimate on how long it will take to perform the evaluation.
     * printEvaluation should call revealer.tick() exactly this many times.
     * @return the ticker length
     */
    public int getTickerSize() {
        return (targetMatch != CompFilter.NONE) ? level : level+1;
    }
    
    /**
     * @inheritDoc
     */
    public boolean isPrintingHeaders() {
        return headers;
    }
    

    /**
     * Print out the results of this evaluation.
     *
     * Since the evaluation operates on a CompMatrix level, the Evaluation.Information
     * being passed back allows totals to be printed out at the end.
     * @see Evaluation#printEvaluation()
     */
    public Evaluation.Information printEvaluation() {
        if (headers) {
            printHeader();
        }

        boolean continuable = true;
        
        for (int i=0; (i<=level); i++) {
            if (continuable) {
                continuable = mat.bringToLevel (i, epf.getScopeRulesFor (this));
                mat.printCurrentFM (output, raw, descriptors.keySet());
            }
            if (ticker != null) ticker.tick();
        }

        //--> Remove all but the best matches, performing aggregation if requested
        if (targetMatch != CompFilter.NONE) {
            if (continuable) {
                mat.removeDuplicates (targetMatch);
                mat.printCurrentFM (output, raw, descriptors.keySet());
            }
            if (ticker != null) ticker.tick();
        }

        ObjectInformation sum = new ObjectInformation();
        if (output != null) {
			output.print (StringHelp.banner("DETECTION(S)", 53));
	}
        mat.printCandidates (output, raw, descriptors.keySet());
        for (Iterator<DescPrototype> iter = descriptors.keySet().iterator(); iter.hasNext(); ) {
            Descriptor curr = iter.next();
            PrecisionRecall pr = new PrecisionRecall ();
            mat.addPRInfo (curr, pr);
            sum.setPrecisionRecall (curr.getType() + " " + curr.getName(), pr);
        }
        if (headers) {
            if (sum != null) {
                printFooter (sum);
            } else if (raw != null) {
                raw.println ("\n#END_SUMMARY");
            }
        }
        return sum;
    }

    /**
     * @inheritDoc
     */
    public void printFooter(Evaluation.Information total) {
        if (output != null) {
			output.print (StringHelp.banner("SUMMARY RESULTS", 53));
            output.println (total.toVerbose());
        }
        if (raw != null) {
            raw.println ("#END_RESULTS\n\n// \n// \n// \n#BEGIN_SUMMARY");
            raw.println (total.toString());
            raw.println ("\n#END_SUMMARY");
        }
    }
    
    /**
     * @inheritDoc
     */
    public void printHeader() {
        if (output != null) {
			output.print (StringHelp.banner("OBJECT EVALUATION", 53));
        }
        if (raw != null) {
            raw.println ("#BEGIN_RESULTS");
        }
    }
    
    /**
     * @inheritDoc
     */
    public void setMatrix(CompMatrix cm) {
        this.mat = cm;
    }
    
    /**
     * @inheritDoc
     */
    public void setOutput(PrintWriter output) {
        this.output = output;
    }
    
    /**
     * @inheritDoc
     */
    public void setPrintingHeaders(boolean on) {
        this.headers = on;
    }
    
    /**
     * @inheritDoc
     */
    public void setRaw(PrintWriter raw) {
        this.raw = raw;
    }
    
    /**
     * @inheritDoc
     */
    public void setTicker(Revealer ticker) {
        this.ticker = ticker;
    }

    /**
     * Return a map of DescPrototypes to their evaluations.
     * @see Evaluation#getMeasureMap()
     */
    public Map<DescPrototype, Map<String, AttrMeasure>> getMeasureMap() {
        return descriptors;
    }
    
    /**
     * @inheritDoc
     * @return <q>Object Evaluation</q>
     */
    public String getName() {
        return "Object Evaluation";
    }
    
    private CompMatrix mat;
    private PrintWriter output;
    private boolean headers;
    private PrintWriter raw;
    private Revealer ticker;

    private int level;
    private int targetMatch;
}
