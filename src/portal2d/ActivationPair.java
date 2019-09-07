/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import java.util.ArrayList;
/**
 *
 * @author Nathan
 */
public class ActivationPair {
    private ArrayList<ActivatableInput> inputs;
    private ActivatableEntity output;
    private boolean previousInput;
    
    public ActivationPair(ActivatableInput input, ActivatableEntity output) {
        this.inputs = new ArrayList<>();
        inputs.add(input);
        this.output = output;
        previousInput = input.readInput();
    }
    
    public ActivatableEntity getOutput() {
        return output;
    }
    
    public ActivatableInput getInput() {
        return inputs.get(0);
    }
    
    public void addInput(ActivatableInput input) {
        inputs.add(input);
    }
    
    public void updatePair() {
        boolean input = false;
        for(ActivatableInput a : inputs) {
            input = input || a.readInput();
        }
        
        if(input != (previousInput)) {
            if(input) {
                output.turnOn();
            }
            else
                output.turnOff();
        }
        previousInput = input;
    }
            
}
