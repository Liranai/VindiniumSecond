package vindinium;

/**
 * Function that takes exactly one argument and return one value.
 *
 * @param A Input/argument type
 * @param B Output/return type
 */
public interface UnaryFunction<A,B> {

    /**
     * Apply function to given |argument|.
     *
     * @param argument Input value
     */
    public B apply(A argument);

} // end of interface UnaryFunction
