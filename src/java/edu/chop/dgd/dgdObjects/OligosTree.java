package edu.chop.dgd.dgdObjects;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jayaramanp on 11/9/16.
 */
public class OligosTree <OligoObject> implements TreeNode{

        OligoObject data;
        OligosTree<OligoObject> parent;
        List<OligosTree<OligoObject>> children;

        public OligosTree(OligoObject data) {
            this.data = data;
            this.children = new LinkedList<OligosTree<OligoObject>>();
        }

        public OligosTree<OligoObject> addChild(OligoObject child) {
            OligosTree<OligoObject> childNode = new OligosTree<OligoObject>(child);
            childNode.parent = this;
            this.children.add(childNode);
            return childNode;
        }

    @Override
    public OligosTree getChildAt(int childIndex) {
        return null;
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public OligosTree getParent() {
        return null;
    }

    @Override
    public int getIndex(TreeNode node) {
        return 0;
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public Enumeration children() {
        return null;
    }

    // other features ...




}
