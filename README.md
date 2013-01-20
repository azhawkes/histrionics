# Histrionics - Simple undo/redo history chains for Java apps

Implementing undo and redo operations in a client-side Java app can be 
a little trickier than you might expect. It can be especially tricky when 
building a graphical editor like the one in [Loadster](http://www.loadsterperformance.com).

This project provides a simple framework for implementing your own 
undoable actions (`HistoryAction`, `MementoAction`) and chaining them together
(`SimpleHistoryChain`, `SwappingHistoryChain`).

## Creating a simple HistoryAction

A `HistoryAction` has to implement 3 methods: `run`, `undo`, and `redo`.

The `run` method works just like a `java.util.Runnable` -- implement
it to perform the action you want.

The `undo` method is responsible for restoring the domain model to its 
previous state. Keep in mind actions will normally be chained together,
so it's important that this method not make any assumptions about what
external changes could have happened to the domain model.

The `redo` method re-applies the changes after they have been undone. For
simple cases, this might just call the `run` method but not always.

Here's a really simple example of a `HistoryAction` that adds/subtracts a random
amount from a random index of an array. See it in action in 
[HistoryChainTest.java](https://github.com/azhawkes/histrionics/blob/master/src/test/com/andyhawkes/histrionics/HistoryChainTest.java).
	
	public class RandomOperationAction implements HistoryAction {
		int index = (int) (Math.random() * testData.length);
		int shift = (int) (Math.random() * 6) - 3;

		public void run() {
			workingData[index] += shift;
		}

		public void undo() {
			workingData[index] -= shift;
		}

		public void redo() {
			workingData[index] += shift;
		}
	}
	
## Using a MementoAction

For more complicated UIs, with lots of possible user actions, crafting a `HistoryAction` for every
operation gets really tedious and error-prone. For those situations there is a `MementoAction` 
that basically freezes a copy of your model before and after every change, and can thaw it out later
to restore the state. You just have to implement two methods, `captureState` and `restoreState`.

Check out [HistoryChainTest.java](https://github.com/azhawkes/histrionics/blob/master/src/test/com/andyhawkes/histrionics/HistoryChainTest.java)
for a simple example of a `MementoAction`. This particular example uses Java serialization to store 
the model state, but you could certainly use whatever approach makes sense for your situation.

If you are implementing many different actions, you'll want to create your own abstract
implementation of `MementoAction` that takes care of capturing and restoring the state, and then 
just override the `runInternal` method for each unique operation.


    public abstract class MyMementoAction extends MementoAction {
        protected abstract void runInternal();
  
        protected byte[] captureState() throws IOException {
            ByteArrayOutputStream state = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(state);
  
            output.writeObject(workingData);
            output.close();
  
            return state.toByteArray();
        }
  
        protected void restoreState(byte[] state) throws IOException {
            try {
                ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(state));
                int[] cannedData = (int[]) input.readObject();
  
                for (int i = 0; i < workingData.length; i++) {
                    workingData[i] = cannedData[i];
                }
  
                input.close();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }
  
    public class ShuffleAction extends MyMementoAction {
        protected void runInternal() {
            Collections.shuffle(Arrays.asList(workingData));
        }
    }
  
    public class SortAction extends MyMementoAction {
        protected void runInternal() {
            Collections.sort(Arrays.asList(workingData));
        }
    }
  

While using Java serialization for a simple byte array is overkill, it works great for storing temporary copies
of a more complicated object-oriented domain model.

## History chains

Once you've implemented your actions, chain them together with a history chain. This makes it easy to hook the undo/redo
operations in with your user interface.

    HistoryChain chain = new SimpleHistoryChain();

    // Run an action
    chain.run(new SortAction());

    // Run a different action
    chain.run(new ShuffleAction());

    // Undo the action
    chain.undo();
    
    // Undo another action
    chain.undo();
    
    // Redo the action you just undid
    chain.redo();

You'll also want to call the `canUndo` and `canRedo` methods from your UI, so it knows when to enable or disable
those menu options and shortcuts.

## Memory management

TODO

## License
Copyright (c) 2013 Andy Hawkes

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
