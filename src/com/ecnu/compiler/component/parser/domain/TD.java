package com.ecnu.compiler.component.parser.domain;

import java.util.List;

/**
 * Tree Diagram
 *
 * @author Michael Chen
 * @date 2018-04-24 13:13
 */
public class TD<NodeType> {
    //保存一个根节点
    TNode<NodeType> root;

    //树节点
    static public class TNode<NodeType>{
        //节点内容
        private NodeType content;
        //孩子节点
        private List<TNode> children;

        public NodeType getContent() {
            return content;
        }

        public void setContent(NodeType content) {
            this.content = content;
        }

        public List<TNode> getChildren() {
            return children;
        }

        public void setChildren(List<TNode> children) {
            this.children = children;
        }

        public void addChild(TNode child){
            children.add(child);
        }
    }
}
