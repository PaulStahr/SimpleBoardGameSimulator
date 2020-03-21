/*******************************************************************************
 * Copyright (c) 2019 Paul Stahr
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package util.data;

import java.util.Comparator;
import java.util.List;

public final class BinaryTree<T> {
 	private Node<T> data;
	private final Comparator<T> comparator;
	public BinaryTree(Comparator<T> c){
		this.comparator = c;
	}
	
	public final void add(T object){
		if (data == null)
			data = new Node<T>(object);
		else
			addLocal(data, object);
	}
	
   	private final void addLocal(Node<T> node, T object){
		int comp = comparator.compare(node.data, object);
		if (comp>0){
			if (node.left == null)
				node.left = new Node<T>(object);
			else
				addLocal(node.left, object);
		}else if (comp!= 0){
			if (node.right == null)
				node.right = new Node<T>(object);
			else
				addLocal(node.right, object);  			
		}
	}
   	
   	public final List<T> fill(List<T> list){
   		if (data != null)
   			data.fill(list);
   		return list;
   	}
    
    private static final class Node<T>{
    	private Node<T> right, left;
    	private final T data;
    	
    	public Node(T data){
    		this.data = data;
    	}
    	
    	public final void fill(List<T> list){
    		if (left != null)
    			left.fill(list);
    		list.add(data);
    		if (right != null)
    			right.fill(list);
    	}
    }
}
