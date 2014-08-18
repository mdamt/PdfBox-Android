package org.apache.pdfbox.pdmodel.common.function;

import java.io.IOException;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.pdmodel.common.function.type4.ExecutionContext;
import org.apache.pdfbox.pdmodel.common.function.type4.InstructionSequence;
import org.apache.pdfbox.pdmodel.common.function.type4.InstructionSequenceBuilder;
import org.apache.pdfbox.pdmodel.common.function.type4.Operators;

/**
 * This class represents a type 4 function in a PDF document.
 * <p>
 * See section 3.9.4 of the PDF 1.4 Reference.
 *
 * @version $Revision: 1.2 $
 */
public class PDFunctionType4 extends PDFunction
{

    private static final Operators OPERATORS = new Operators();

    private final InstructionSequence instructions;

    /**
     * Constructor.
     *
     * @param functionStream The function stream.
     * @throws IOException if an I/O error occurs while reading the function
     */
    public PDFunctionType4(COSBase functionStream) throws IOException
    {
        super( functionStream );
        this.instructions = InstructionSequenceBuilder.parse(
                getPDStream().getInputStreamAsString());
    }


    /**
     * {@inheritDoc}
     */
    public int getFunctionType()
    {
        return 4;
    }

    /**
    * {@inheritDoc}
    */
    public float[] eval(float[] input) throws IOException
    {
        //Setup the input values
        ExecutionContext context = new ExecutionContext(OPERATORS);
        for (int i = 0; i < input.length; i++)
        {
            PDRange domain = getDomainForInput(i);
            float value = clipToRange(input[i], domain.getMin(), domain.getMax());
            context.getStack().push(value);
        }

        //Execute the type 4 function.
        instructions.execute(context);

        //Extract the output values
        int numberOfOutputValues = getNumberOfOutputParameters();
        int numberOfActualOutputValues = context.getStack().size();
        if (numberOfActualOutputValues < numberOfOutputValues)
        {
            throw new IllegalStateException("The type 4 function returned "
                    + numberOfActualOutputValues
                    + " values but the Range entry indicates that "
                    + numberOfOutputValues + " values be returned.");
        }
        float[] outputValues = new float[numberOfOutputValues];
        for (int i = numberOfOutputValues - 1; i >= 0; i--)
        {
            PDRange range = getRangeForOutput(i);
            outputValues[i] = context.popReal();
            outputValues[i] = clipToRange(outputValues[i], range.getMin(), range.getMax());
        }

        //Return the resulting array
        return outputValues;
    }
}
