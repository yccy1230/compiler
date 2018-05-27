package com.ecnu.compiler.component.lexer.domain;

import com.ecnu.compiler.component.lexer.domain.base.Graph;
import com.ecnu.compiler.component.lexer.domain.graph.Edge;
import com.ecnu.compiler.component.lexer.domain.graph.State;
import com.ecnu.compiler.component.lexer.domain.re2dfaUtils.DfaState;

import java.io.IOException;
import java.util.*;

/**
 * 确定性有限状态自动机
 * @author Michael Chen
 */
public class DFA extends Graph {

    static public ArrayList<DFA> buildDFAListFromREList(List<RE> reList){
        ArrayList<DFA> dfaList = new ArrayList<>();
        //使用直接构造
        for (RE re : reList){
            dfaList.add(re.getDFADirectly());
        }

        //最小化状态
        for (DFA dfa : dfaList){
            DFA2MinDFA(dfa);
        }
        return dfaList;
    }

    private String name;

    private List<DfaState> statesList;

    private List<State> endStates;

    private State startDfaState;

    private DfaState startState;

    private List<DfaState> endStateList;

    public List<List<TransitMat>> stateTransitionMat;

    public DFA() { }

    public DFA(List<DfaState> dfaStateList,List<List<TransitMat>> stateTransitionMat) {
        this.setStateList(dfaStateList);
        this.stateTransitionMat = stateTransitionMat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<State> getStates() {
        return stateList;
    }

    public void setStates(List<State> states) {
        this.stateList = states;
    }

    public State getStartDfaState() {
        return startDfaState;
    }

    public void setStartDfaState(State startDfaState) {
        this.startDfaState = startDfaState;
    }

    public List<DfaState> getDfaStateList() {
        return statesList;
    }

    public void setStateList(List<DfaState> dfaStateList) {
        this.statesList = dfaStateList;
    }

    public void setStartState(DfaState startState) {
        this.startState = startState;
    }

    public void setEndStateList(List<DfaState> endStateList) {
        this.endStateList = endStateList;
    }

    public State take(char input){
        return null;
    }


    public void print() {
        String states = "States:\n";
        String edges = "Edges:\n";
        String endStates = "End States:\n";
        for(DfaState s : statesList) {
            states += s.toString();
            for(Edge e : s.getEdgeList()){
                edges += e.toStringForDfa();
            }
        }
        for(DfaState s : endStateList) {
            endStates = endStates + s.getName() + "\n";
        }
        System.out.println(states);
        System.out.println(edges);
        System.out.println("Start State:\n" + startState.getName());
        System.out.println(endStates);
    }

    public int getMaxId(){
        int maxId=0;
        for(State s : this.stateList){
            if(s.getId()>maxId){
                maxId=s.getId();
            }
        }
        return maxId;
    }

    public State getStateById(int id) {
        for (State state : stateList) {
            if (state.id == id) {
                return state;
            }
        }
        return null;
    }

    public List<State> getEndStates() {
        return endStates;
    }

    public void setEndStates(State endState) {
        this.endStates = new ArrayList<>();
        this.endStates.add(endState);
    }

    /**
     * match
     * @param lexeme 希望匹配的源代码
     * @return 如果匹配成功，则返回匹配路径的列表；如果匹配失败，则返回NULL
     * @author Lucto
     */
    public List<Integer> match(String lexeme){
        //todo 判断本DFA是否匹配RE
        if(lexeme == null) {
            return null;
        }
        List<Boolean> success = new ArrayList<>();
        success.add(false);
        List<Integer> path = new ArrayList<>();
        path.clear();
        char[] chars = lexeme.toCharArray();
        //开始匹配
        startMatch(chars,0,path,startDfaState,success);
        //匹配成功
        if(success.get(0)){
            return path;
        }else{
            return null;
        }
    }

    /**
     * startMatch
     * @param chars 希望匹配的单词序列
     * @param cur 当前匹配到的单词
     * @author Lucto
     */
    private void startMatch(char[] chars,int cur,List<Integer> path,State currentState,List<Boolean> success){
        //已经遍历完chars
        if(cur >= chars.length) {
            //到达dfa的结束状态
            if(endStates.contains(currentState)) {
                //表示已成功匹配
                path.add(currentState.getId());
                success.remove(0);
                success.add(true);
            }
        }else {
            List<Edge> edgeList = currentState.getEdgeList();
            if(!edgeList.isEmpty()) {
                for (Edge edge : edgeList) {
                    if (chars[cur] == edge.getWeight()) {
                        path.add(currentState.getId());
                        startMatch(chars, cur + 1, path, edge.getEndState(), success);
                        if (success.get(0)) {
                            return;
                        } else {
                            path.remove(path.size() - 1);
                        }
                    }
                }
            }
        }
    }


    /**
     * minimize DFA
     *
     * @param dfa DFA
     * @return DFA
     * @author Chen Jianing
     **/

    //todo 现在最小化会在输入里面操作，要做成构造一个新的。
    public static DFA DFA2MinDFA(DFA dfa){
        //GetTransitMat(dfa);
        dfa.stateTransitionMat = GetTransitMat(dfa);
        //用于存放最小化的过程中产生的状态划分
        List<List<State>> stateListsPartition = new ArrayList<List<State>>();
        //phrase 1: 将化简前的DFA的状态分为非可接受状态和可接受状态两部分
        List<State> nonTerminalStates = new ArrayList<State>();
        List<State> copyOfOriginalState = CloneOfStateList(dfa.stateList);
//        System.out.println(copyOfOriginalState);
//        for(State state:dfa.endStates){
//            System.out.print(state.getId());
//        }
        for (State state : copyOfOriginalState) {
            for(State endState : dfa.endStates){
                if(state.getId()!=endState.getId()) {
                    nonTerminalStates.add(state);
                }
            }
        }
//            if (!dfa.endStates.contains(state)) {
//                nonTerminalStates.add(state);
//                System.out.print("State"+" = "+state+" ");
//                System.out.println("endStates"+" = "+dfa.endStates+ " ");
//            }
//        }
        for (State state : copyOfOriginalState) {
            System.out.print(state.getId() + " ");
        }
        System.out.println();
        for (State state : dfa.endStates) {
            System.out.print(state.getId() + " ");
        }
        System.out.println();
        for (State state : nonTerminalStates) {
            System.out.print(state.getId() + " ");
        }

        List<State> terminalStates = CloneOfStateList(dfa.endStates);
        stateListsPartition.add(nonTerminalStates);
        stateListsPartition.add(terminalStates);

        // phrase 2: 看nonTerminalStates能否再分,如果可以，则进行划分
        int index = stateListsPartition.size() - 1;
        //存储不在属于当前划分的状态
        List<State> states = new ArrayList<State>();
        List<State> statesToRemove = new ArrayList<State>();
        states = dfa.stateList;

        Set<Character> alphabetSet = new HashSet<>();
        int count = 0;
        for (State state : dfa.getStates()) {                //获得所有list及set的值
            for (Edge edge : state.getEdgeList()) {
                alphabetSet.add(edge.getWeight());
                count++;
            }
        }
        int a = alphabetSet.size();
        for (int i = 0; i < a; i++) {
            for (State state : nonTerminalStates) {
                int stateIndex = getIndexOf(states, state);
                //System.out.println(stateIndex);
                //获取状态state遇到下标为i的符号时状态跳转情况
//                TransitMat transitEleRow = stateTransitionMat.get(stateIndex).get(i);
//                System.out.println(transitEleRow.getStateIndex());
//                State nextState = states.get(transitEleRow.getStateIndex());
                TransitMat currState = dfa.stateTransitionMat.get(stateIndex).get(i);
                if(currState.getStateIndex() == -1){
                    continue;
                }
                State nextState = states.get(currState.getStateIndex());
                if (!nonTerminalStates.contains(nextState)) {
                    //经过状态转移达到的状态不包含在状态集合statesToCheck中，
                    //则nonTerminalStates继续划分为state和去掉state之后的nonTerminalStates
                    stateListsPartition.add(stateListsPartition.size() - 1, Arrays.asList(state));
                    statesToRemove.add(state);
                    if (index > stateListsPartition.size() - 1) {
                        index = stateListsPartition.size() - 1;
                    }
                }
            }
        }
        nonTerminalStates.removeAll(statesToRemove);  //移除不再属于当前划分的状态


        // phrase 3: 看terminalStates能否再分，如果可以，则进行划分
        index = stateListsPartition.size() - 1;
        statesToRemove.clear();

        for (int i = 0; i < a; i++) {
            for (State state : terminalStates) {
                int stateIndex = getIndexOf(states, state);
                //获取状态state遇到下标为i的符号时状态跳转情况

//                TransitMat transitEleRow =
//                        stateTransitionMat.get(stateIndex).get(i);
//                State nextState = states.get(transitEleRow.getStateIndex());
                TransitMat currState = dfa.stateTransitionMat.get(stateIndex).get(i);
                if(currState.getStateIndex() == -1){
                    continue;
                }
                State nextState = states.get(currState.getStateIndex());
                if (!terminalStates.contains(nextState)) {
                    //经过状态转移达到的状态不包含在状态集合statesToCheck中，
                    //则nonTerminalStates继续划分为state和去掉state之后的nonTerminalStates
                    stateListsPartition.add(stateListsPartition.size() - 1, Arrays.asList(state));
                    statesToRemove.add(state);
                    if (index > stateListsPartition.size() - 1) {
                        index = stateListsPartition.size() - 1;
                    }
                }
            }
        }
        terminalStates.removeAll(statesToRemove);  //移除不再属于当前划分的状态

        int leftMostEndStateIndex = index;

        // phrase 4: 根据存储状态列表的列表的每一个元素作为一个状态，构造最小化DFA
        rebuildDFAWithSimplifiedStateList(dfa,stateListsPartition, leftMostEndStateIndex);
        return dfa;
    }

    private static int getIndexOf(List<State> states, State state) {
        int i = 0;
        for (State cmpState : states) {
            if (cmpState.getId() == state.getId()) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static List<List<TransitMat>> GetTransitMat(DFA dfa){
        Set<Character> alphabetSet = new HashSet<>();
        List<Integer> startOfEdgeList = new ArrayList<>();
        List<Integer> endOfEdgeList = new ArrayList<>();
        List<Character> alphabetList = new ArrayList<>();
        int count = 0;                                       //边的数量
        for (State state : dfa.getStates()) {                //获得所有list及set的值
            for (Edge edge : state.getEdgeList()) {
                alphabetSet.add(edge.getWeight());
                startOfEdgeList.add(edge.getStartState().getId());
                endOfEdgeList.add(edge.getEndState().getId());
                alphabetList.add(edge.getWeight());
                count++;
            }
        }
        List<Character> alphaSetList = new ArrayList<Character>();          //不重复的alphabet
        alphaSetList.addAll(alphabetSet);
        /*开始构建矩阵*/
        int a = dfa.getStates().size();
        int b = alphabetSet.size();
        System.out.print(a+" "+b+'\n');
        Integer stateTable[][] = new Integer[a][b];
        for (int i = 0; i < a; i++) {                                         //初始化数组
            for (int j = 0; j < b; j++) {
                stateTable[i][j] = -1;
            }
        }

        int i = 0;
        int j, flag;
        for (int temp = 0; temp < count; temp++) {
            j = 0;
            flag = 0;
            while (startOfEdgeList.get(temp) == i) {
                if (alphaSetList.get(j).equals(alphabetList.get(temp))) {
                    stateTable[i][j] = endOfEdgeList.get(temp);
                    flag = 1;
                    break;
                }
                j++;
            }
            if (temp != count - 1 && !(flag == 1 && startOfEdgeList.get(temp + 1) == i)) {
                i++;
            }
        }
        for (i = 0; i < a; i++) {
            for (j = 0; j < b; j++) {
                System.out.print(stateTable[i][j] + " ");     //output
            }
            System.out.print('\n');
        }
        /**********getMatrix()********************************/
        List<List<TransitMat>> stateTransitionMat = new ArrayList<List<TransitMat>>();
        for (i = 0; i < dfa.getStates().size(); i++) {
            stateTransitionMat.add(new ArrayList<TransitMat>());
            for (j = 0; j < alphabetSet.size(); j++) {
                int stateIndex = stateTable[i][j];
                TransitMat transitEle =
                        new TransitMat(j, stateIndex);
                stateTransitionMat.get(i).add(transitEle);
            }
        }
        return stateTransitionMat;
    }

    public static List<State> CloneOfStateList(List<State> states) {
        List<State> copyOfStateList = new ArrayList<State>();
        for (State state : states) {
            // 不用FAState copyOfState = new FAState(state.getId()); (浅度复制String对象)
            State copyOfState = new State(new Integer(state.getId()), false);
            copyOfStateList.add(copyOfState);
        }
        return copyOfStateList;
    }

    /**
     * 根据存储状态列表的列表的每一个元素作为一个状态，构造最小化DFA
     * @param stateLists 存储状态列表的列表
     */

    private static void rebuildDFAWithSimplifiedStateList(DFA dfa,List<List<State>> stateLists,int leftMostEndStateIndex) {
        List<State> copyOfStates = CloneOfStateList(dfa.stateList);
//        System.out.println(copyOfStates);
        dfa.stateList.clear();
        List<List<TransitMat>> copyOfTransitMat = deepCloneOfStateTransitionMat(dfa.stateTransitionMat);
        System.out.println(copyOfTransitMat.size());
        dfa.stateTransitionMat.clear();

        // phrase 1: 重新构造状态列表
        rebuildStateList(dfa, stateLists, leftMostEndStateIndex);

        // phrase 2: 重新构建状态转移矩阵
        rebuildStateTransitMat(dfa, copyOfStates, copyOfTransitMat, stateLists);
    }

    private static List<List<TransitMat>> deepCloneOfStateTransitionMat(List<List<TransitMat>> stateTransitionMat) {
        List<List<TransitMat>> copyOfStateTransitionMat =
                new ArrayList<List<TransitMat>>();
        for(List<TransitMat> rowOfStateTransitMat: stateTransitionMat) {
            List<TransitMat> copyOfStateTransitList =
                    new ArrayList<TransitMat>();
            for(TransitMat transitEle: rowOfStateTransitMat) {
                TransitMat copyOfTransitEle =
                        new TransitMat(transitEle.getAlphaIndex(),transitEle.getStateIndex());
                copyOfStateTransitList.add(copyOfTransitEle);
            }
            copyOfStateTransitionMat.add(copyOfStateTransitList);
        }
        return copyOfStateTransitionMat;
    }


//    /**
//     * 判断一个状态列表能否再分，如果可以，则继续划分该列表为多个列表
//     * @param stateLists 存储划分得到的状态列表的列表
//     * @param statesToCheck 待划分状态列表
//     */
//    private int splitStateListIfCould(List<List<State>> stateLists,
//                                      List<State> statesToCheck) {
//        int index = stateLists.size() - 1;
//        //存储不在属于当前划分的状态
//        List<State> statesToRemove = new ArrayList<State>();
//        for(int i=0; i<this.alphabet.size(); i++) {
//            for(State state : statesToCheck) {
//                int stateIndex = this.states.indexOf(state);
//                //获取状态state遇到下标为i的符号时状态跳转情况
//                TransitMat transitEleRow =
//                        this.stateTransitionMat.get(stateIndex).get(i);
//                State nextState = this.states.get(transitEleRow.getStateIndex());
//                if(!statesToCheck.contains(nextState)) {
//                    //经过状态转移达到的状态不包含在状态集合statesToCheck中，
//                    //则nonTerminalStates继续划分为state和去掉state之后的nonTerminalStates
//                    stateLists.add(stateLists.size()-1, Arrays.asList(state));
//                    statesToRemove.add(state);
//                    if(index > stateLists.size() - 1) {
//                        index = stateLists.size() - 1;
//                    }
//                }
//            }
//        }
//        statesToCheck.removeAll(statesToRemove);  //移除不再属于当前划分的状态
//        return index;
//    }

    /**
     * 重新构造状态列表
     * @param stateLists 包含状态列表的列表
     * @param leftMostEndStateIndex 可接受状态中下标最小的状态在stateLists中的下标
     */
    private static void rebuildStateList (DFA dfa,List<List<State>> stateLists,
                                          int leftMostEndStateIndex){
        Random random = new Random();
        //stateLists中的第一个元素中的所有状态构成新的DFA对象的开始状态
        dfa.startDfaState =
                new State(random.nextInt(),false);
        dfa.stateList.add(dfa.startDfaState);

        //添加既不是开始状态节点，也不是可接受状态节点的状态节点
        for (int i = 1; i < leftMostEndStateIndex; i++) {
            State newState =
                    new State(random.nextInt(),false);
            dfa.stateList.add(newState);
        }

        // stateLists中原来DFA对象的可接受状态构成新的DFA对象的可接受状态
        for (int i = leftMostEndStateIndex; i < stateLists.size(); i++) {
            State newState =
                    new State(random.nextInt(),true);
            dfa.endStates.add(newState);
            dfa.stateList.add(newState);
        }
    }

    /**
     * 为最小化DFA重新构造状态转移矩阵
     * @param originalStateList 原来DFA的状态列表
     * @param originalStateTransitMat 原来DFA的状态转移矩阵
     * @param stateLists 存储状态列表的列表
     */
    private static void rebuildStateTransitMat(DFA dfa,List<State> originalStateList,
                                               List<List<TransitMat>> originalStateTransitMat,
                                               List<List<State>> stateLists) {
        for(int i=0; i<stateLists.size(); i++) {
            List<State> stateList = stateLists.get(i);		//当前状态划分
            List<TransitMat> stateTransitEleRow =
                    new ArrayList<TransitMat>();
            //建立到其它划分中状态的状态转移
            buildTransitWithStatesInOtherPartition(
                    originalStateList,originalStateTransitMat,
                    stateLists, stateList, stateTransitEleRow);
            //建立划分内的状态转移(针对划分内某个状态遇到某一符号串不转向下一状态的情况)
            buildTransitWithStatesInInnerPartition(
                    originalStateList, originalStateTransitMat,
                    stateLists, stateList, stateTransitEleRow);
            dfa.stateTransitionMat.add(stateTransitEleRow);
        }
    }

    //建立划分内的状态转移(针对划分内某个状态遇到某一符号串不转向下一状态的情况)
    private static void buildTransitWithStatesInInnerPartition(
            List<State> originalStateList,
            List<List<TransitMat>> originalStateTransitMat,
            List<List<State>> stateLists, List<State> stateList,
            List<TransitMat> stateTransitEleRow) {
        for(State stateInPartition : stateList) {
            int stateIndex = originalStateList.indexOf(stateInPartition);
            List<TransitMat> transitEleRow =
                    originalStateTransitMat.get(stateIndex);
            for(TransitMat transitEle : transitEleRow) {
                if(transitEle.getStateIndex() == stateIndex) { //在该状态上存在转向自己的循环
                    //获取在最小化的DFA对象中的下标
                    int currentStateIndex =
                            getStateIndexInNewDFA(stateLists, stateInPartition);
                    stateTransitEleRow.add(
                            new TransitMat(transitEle.getAlphaIndex(), currentStateIndex));
                }
            }
        }
    }

    //建立到其它划分中状态的状态转移
    private static void buildTransitWithStatesInOtherPartition(
            List<State> originalStateList,
            List<List<TransitMat>> originalStateTransitMat,
            List<List<State>> stateLists, List<State> stateList,
            List<TransitMat> stateTransitEleRow) {
//        for(int i=0 ; i<stateList.size() ; i++){
//            System.out.println("**");
//            stateList.get(i).outputId();
//            System.out.println("**");
//        }
        int originalStateIndex = originalStateList.indexOf(stateList.get(0));
        List<TransitMat> stateTransitMatRow =
                originalStateTransitMat.get(originalStateIndex);
        for(TransitMat transitEle : stateTransitMatRow) {
            //当前转向的状态
            State currentState = originalStateList.get(transitEle.getStateIndex());
            if(!stateList.contains(currentState)) {
                //不是在同一个划分中，存在到其它划分则状态的状态转移
                int currentStateIndex = getStateIndexInNewDFA(stateLists, currentState);
                stateTransitEleRow.add(
                        new TransitMat(transitEle.getAlphaIndex(), currentStateIndex));
            }
        }
    }

    /**
     * 查找某一状态在最小化DFA中所在划分的下标
     * @param stateLists 状态列表的划分
     * @param state 将要查找的FAState对象
     * @return 某一状态在最小化DFA中所在划分的下标
     */
    private static int getStateIndexInNewDFA(List<List<State>> stateLists, State state) {
        for(int i=0; i<stateLists.size(); i++) {
            List<State> currentStateList = stateLists.get(i);
            if(currentStateList.contains(state)) {
                return i;
            }
        }
        return -1;
    }
}
