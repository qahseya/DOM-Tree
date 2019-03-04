package structures;

import java.util.*;

/**
 * This class implements an HTML DOM Tree. Each node of the tree is a TagNode, with fields for
 * tag/text, first child and sibling.
 * 
 */
public class Tree {

	/**
	 * Root node
	 */
	TagNode root=null;

	/**
	 * Scanner used to read input HTML file when building the tree
	 */
	Scanner sc;


	/**
	 * Initializes this tree object with scanner for input HTML file
	 * 
	 * @param sc Scanner for input HTML file
	 */
	public Tree(Scanner sc) {
		this.sc = sc;
		root = null;
	}
	///////////// HELPER METHODS \\\\\\\\\\\\\\\

	/*
	 * This method merely removes the brackets off of the tag.
	 */
	private static String removeBrackets (String tag) {
		String without = new String (tag.substring(1, tag.length()-1));
		return without;
	}

	/*
	 * This method changes the tag to an end tag so that you can store it in the stack.
	 */
	private static String makeEndTag (String tag) {
		String endTag = new String ("</" + tag.substring(1, tag.length()));
		return endTag;
	}

	private void replaceHelper (String oldTag, String newTag, TagNode babyRoot) {
		/*
		 * base case- when u get to the point where root is null/ when u actually find the old tag
		 * looking for instance where u find the oldTag, in which case u simply replace it
		 * replace horizontally and vertically by calling on children & siblings
		 * 
		 */
		if (babyRoot == null) {
			return;

			/*
			 * this is the only difference from a regular traversal. 
			 * it replaces the tag, and then moves along
			 * once it hits null, it will return the tree with modification that were made.
			 * 
			 */
		} else if (babyRoot.tag.equals(oldTag)) { 
			babyRoot.tag = newTag;
		}



		replaceHelper (oldTag, newTag, babyRoot.firstChild);
		replaceHelper (oldTag, newTag, babyRoot.sibling);

	}

	private TagNode findTable( TagNode babyRoot) {
		//traverse through the entire table looking for local root.
		//2 base cases: if there IS a table and if there IS not
		//when u find a table, return where the table itself is so you can modify it in bold method

		if (babyRoot == null) //BASE CASE. IF THERE'S NO TABLE IN THE WHOLE TREE
			return null;

		
		if (babyRoot.tag.equals("table")) //BASE CASE. ONCE U FIND THE TREE
			return babyRoot;

		TagNode sibling = findTable (babyRoot.sibling); //MOVE TO THE SIDE
		TagNode first = findTable (babyRoot.firstChild); //MOVE DOWN


		if (sibling != null) 
			return sibling;

		if (first != null)
			return first;

		return null;
	}
	
	private void removeTag1 (TagNode newRoot, String tag) {
		
		if (newRoot == null) { //WHEN U HIT THE END/ BASE CASE
			return;
		}

		if ( newRoot.firstChild != null && newRoot.tag.equals(tag)) { //
			newRoot.tag = newRoot.firstChild.tag; //change the tags
			if (newRoot.firstChild.sibling != null) {
				TagNode traverseThru = null;
				traverseThru = newRoot.firstChild;
				while ( traverseThru.sibling != null) {
					traverseThru.sibling = newRoot.sibling;
					traverseThru = traverseThru.sibling;
				}
				newRoot.sibling = newRoot.firstChild.sibling;
			}
			newRoot.firstChild = newRoot.firstChild.firstChild; //link across
		}
		removeTag1 (newRoot.firstChild, tag);
		removeTag1 (newRoot.sibling, tag);
	}

	private void removeTag2 (TagNode localRoot, String tag) {
		if (localRoot == null)
			return;

		if (localRoot.tag.equals(tag) && localRoot.firstChild != null) {
			localRoot.tag = "p"; //change to p tag condition

			TagNode lookThru = null;
			//System.out.println();
			lookThru = localRoot.firstChild; //use this to traverse.
			while (lookThru.sibling != null) {
				lookThru.tag = "p";
				lookThru = lookThru.sibling;
			}
			lookThru.sibling = localRoot.sibling; //relink
			localRoot.sibling = localRoot.firstChild.sibling; //link sideways
			localRoot.firstChild = localRoot.firstChild.firstChild;
		}

		removeTag2(localRoot.firstChild, tag);
		removeTag2(localRoot.sibling, tag);

	}


	/**
	 * Builds the DOM tree from input HTML file, through scanner passed
	 * in to the constructor and stored in the sc field of this object. 
	 * 
	 * The root of the tree that is built is referenced by the root field of this object.
	 */
	public void build() {
	
		/* 
		 * you cant traverse backwards. 
		 * while the NEXXT LINE isn't the html end tag ===> you're going to keep building
		 * root is the html tag, which is going to be the first input into the program.
		 * 
		 * i'm gonna come across 3 different types of nodes: a <tag>, a </tag>, or plain text.
		 * 
		 * whenever I come across a plain text:
		 * it's gonna be a child of the previous node. and every line that 
		 * follows will be a sibling node to the plain text UNTIL i hit the tag before the plain text.  
		 * if the next line is a tag and it's NOT the prev tag: go down.
		 * 
		 * whenever i come across a </tag>:
		 * 1. I will NOT insert this anywhere. this is a signal to move UP. 
		 * problem: i cannot move UP.
		 * solution: leave a pointer node at the opening of the tag. travel back UP to the pointer node
		 * and see what happens.
		 * 
		 * whenever i come across a <tag>:
		 * i'm going to just automatically make a child node of the innards of the node.
		 * push this node into stack after putting it 
		 */
		boolean isTag = false; //this is how i will differentiate between plain text & reg tags
		Stack <TagNode> endTags = new Stack <TagNode>();

		if ( !sc.hasNextLine()) {
			return;
		}

		TagNode html = new TagNode ("html", null, null);
		TagNode body = new TagNode ("body", null, null);
		root = html; root.firstChild = body;
		endTags.push(root); endTags.push(body);

		sc.nextLine(); sc.nextLine();

		while ( sc.hasNext() == true ) { 
			String temp = sc.nextLine();
			isTag = false;

			if (temp.charAt(0) == '<' && temp.charAt(1) != '/') { //if it's a beginning tag
				temp = removeBrackets (temp);
				isTag = true;
			} else if ( temp.charAt(0) =='<' && temp.charAt(1) == '/') { //if it's an end tag
				endTags.pop();
				continue;
			}

			// i'll hit this point without encountering the if statements if its plain text
			TagNode insert = new TagNode (temp, null, null);

			if (endTags.peek().firstChild != null) {  //as long as there's a first child in there
				TagNode peekChild = endTags.peek().firstChild;
				while ( peekChild.sibling != null) {
					peekChild = peekChild.sibling; //make it the sibling 
				}
				peekChild.sibling = insert;
			} else {
				endTags.peek().firstChild = insert; //make it the first child
			}
			if (isTag == true) { //if it's a beginning tag
				endTags.push(insert);
			}
		}

			
	}

	/**
	 * Replaces all occurrences of an old tag in the DOM tree with a new tag
	 * 
	 * @param oldTag Old tag
	 * @param newTag Replacement tag
	 */

	public void replaceTag(String oldTag, String newTag) {

		replaceHelper ( oldTag, newTag, root );

	}

	/**
	 * Boldfaces every column of the given row of the table in the DOM tree. The boldface (b)
	 * tag appears directly under the td tag of every column of this row.
	 * 
	 * @param row Row to bold, first row is numbered 1 (not 0).
	 */
	public void boldRow(int row) {
		/*
		 * Step 1: find the table
		 * Step 2: find the row within the table by the integer given #
		 * Step 3: find each column WITHIN the row and INDIVIDUALLY put a bold tag before it
		 * it should be <tr> => <td> => <b>
		 * 
		 * after finding the table => simply find the row and implement within this method
		 */
		//TagNode tElement =  null;
		TagNode table = findTable(root);
		if (table == null)
			return;

		TagNode tableRow = table.firstChild;

		int i = 1; //counter to go thru the rows
		while ( i != row) {
			tableRow = tableRow.sibling;
			i++;
		}

		TagNode tElement = tableRow.firstChild;
		while (tElement != null) { //change every single cell's hierarchy. table => row => bold => column tag.
			TagNode embolden = new TagNode ("b", tElement.firstChild, null);
			tElement.firstChild = embolden;
			tElement = tElement.sibling;
			//TagNode tElement = tableRow.firstChild;
		}
		//TagNode tElement = tableRow.firstChild;
	}

	/**
	 * Remove all occurrences of a tag from the DOM tree. If the tag is p, em, or b, all occurrences of the tag
	 * are removed. If the tag is ol or ul, then All occurrences of such a tag are removed from the tree, and, 
	 * in addition, all the li tags immediately under the removed tag are converted to p tags. 
	 * 
	 * @param tag Tag to be removed, can be p, em, b, ol, or ul
	 */
	public void removeTag(String tag) {
		/*
		 * Ok. this is a lot like traversing, so do it recursively.
		 * 2 cases-- 1) with b, em, or p && 2) with ol or ul
		 * 1) just remove the tag. TRAVERSE tree until you reach p, em, or b. link child of p, em, or b to parent
		 * and DELINK the em tag.
		 * 
		 * 2) do the same as 1 EXCEPT extra step is to change to p tags
		 * 
		 */
	

		if((tag.equals("ol") || tag.equals("ul"))){
			removeTag2(root, tag);
		}
		if (tag.equals("p") || tag.equals("em") || tag.equals("b")) {
			removeTag1 (root, tag);
		}
	}

	private void addPrivate (TagNode lRoot, String word, String tag) {
		if (lRoot == null)
			return;

		addPrivate(lRoot.firstChild, word, tag);
		addPrivate(lRoot.sibling, word, tag);

		boolean check = true;

		if (lRoot.firstChild == null) {
			check = false;
			String testIt = lRoot.tag.toLowerCase();
			while (testIt.contains(word)) {
				boolean matchFound = false;
				String wordWTag = "";
				String [] splits = lRoot.tag.split(" ");
				StringBuilder taggerString = new StringBuilder(lRoot.tag.length());
				int words = 0;
				while (words < splits.length) {
					if (splits[words].toLowerCase().matches(word + "[;!:?,.]") ) {
						matchFound = true;
						wordWTag = splits[words];
						int i = words+1;
						while (i < splits.length) {
							taggerString.append(splits[i] + " ");
							i++;
						}
						break;
					}
					words++;

				}
				if (matchFound == false) 
					return;
				String toReturn = taggerString.toString().trim();
				if (check == false) {
					lRoot.tag = tag;
					lRoot.firstChild = new TagNode (wordWTag, null, null);
					if (toReturn.equals("") == false) {
						lRoot.sibling = new TagNode (toReturn, null, lRoot.sibling);
						lRoot = lRoot.sibling;
					}
				} else if (check == true) {
					TagNode newWordNode = new TagNode (wordWTag, null, null);
					TagNode newTag = new TagNode(tag, newWordNode, lRoot.sibling);
					System.out.println(newTag + ": this is the new tag");
					lRoot.sibling = newTag;
					lRoot.tag = lRoot.tag.replaceFirst(" " + wordWTag, "");

					if (toReturn.equals("") == false) {
						lRoot.tag = lRoot.tag.replace(toReturn, "");
						newTag.sibling = new TagNode (toReturn, null, newTag.sibling);
						lRoot = newTag.sibling;
					}

				}
			}
		}
	}
	/**
	 * Adds a tag around all occurrences of a word in the DOM tree.
	 * 
	 * @param word Word around which tag is to be added
	 * @param tag Tag to be added
	 */

	public void addTag(String word, String tag) {
		//MAKE THIS CASE SENSITIVE IT IS NOT YET
		/*
		 * traverse through the tree looking for the word.
		 * once found: 
		 * the word INCLUDES the next punctuation around it.
		 * 
		 * case insensitive-- remember to 
		 * 
		 */
		addPrivate(root, word.toLowerCase(), tag);
	}

	/**
	 * Gets the HTML represented by this DOM tree. The returned string includes
	 * new lines, so that when it is printed, it will be identical to the
	 * input file from which the DOM tree was built.
	 * 
	 * @return HTML string, including new lines. 
	 */
	public String getHTML() {
		StringBuilder sb = new StringBuilder();
		getHTML(root, sb);
		return sb.toString();
	}

	private void getHTML(TagNode root, StringBuilder sb) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			if (ptr.firstChild == null) {
				sb.append(ptr.tag);
				sb.append("\n");
			} else {
				sb.append("<");
				sb.append(ptr.tag);
				sb.append(">\n");
				getHTML(ptr.firstChild, sb);
				sb.append("</");
				sb.append(ptr.tag);
				sb.append(">\n");	
			}
		}
	}

	/**
	 * Prints the DOM tree. 
	 *
	 */
	public void print() {
		print(root, 1);
	}

	private void print(TagNode root, int level) {
		for (TagNode ptr=root; ptr != null;ptr=ptr.sibling) {
			for (int i=0; i < level-1; i++) {
				System.out.print("      ");
			};
			if (root != this.root) {
				System.out.print("|---- ");
			} else {
				System.out.print("      ");
			}
			System.out.println(ptr.tag);
			if (ptr.firstChild != null) {
				print(ptr.firstChild, level+1);
			}
		}
	}
}
