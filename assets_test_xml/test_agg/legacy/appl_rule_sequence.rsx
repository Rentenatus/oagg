<?xml version="1.0" encoding="UTF-8"?>
<Document version="1.0">
    <RuleSequenceApplicability ID="I0">
        <GraphTransformationSystem ID="I1" directed="true"
            name="RuleSequenceTest" parallel="true">
            <TaggedValue Tag="AttrHandler" TagValue="Java Expr">
                <TaggedValue Tag="Package" TagValue="java.lang"/>
                <TaggedValue Tag="Package" TagValue="java.util"/>
            </TaggedValue>
            <TaggedValue Tag="CSP" TagValue="true"/>
            <TaggedValue Tag="injective" TagValue="true"/>
            <TaggedValue Tag="dangling" TagValue="true"/>
            <TaggedValue Tag="identification" TagValue="true"/>
            <TaggedValue Tag="NACs" TagValue="true"/>
            <TaggedValue Tag="PACs" TagValue="true"/>
            <TaggedValue Tag="GACs" TagValue="true"/>
            <TaggedValue Tag="TypeGraphLevel" TagValue="DISABLED"/>
            <Types>
                <NodeType ID="I2" abstract="false" name="Node%:RECT:java.awt.Color[r=0,g=0,b=0]:[NODE]:%:RECT:java.awt.Color[r=0,g=0,b=0]::[NODE]:"/>
                <Graph ID="I3" kind="TG" name="TypeGraph">
                    <Node ID="I4" type="I2"/>
                </Graph>
            </Types>
            <Graph ID="I5" kind="HOST" name="Host">
                <Node ID="I6" type="I2"/>
            </Graph>
            <Rule ID="I7" formula="true" name="rule1">
                <Graph ID="I9" kind="LHS" name="Left">
                    <Node ID="I10" type="I2"/>
                </Graph>
                <Graph ID="I11" kind="RHS" name="Right">
                    <Node ID="I12" type="I2"/>
                </Graph>
                <Morphism name="rule1"/>
                <TaggedValue Tag="layer" TagValue="0"/>
                <TaggedValue Tag="priority" TagValue="0"/>
            </Rule>
            <Rule ID="I13" formula="true" name="rule2">
                <Graph ID="I15" kind="LHS" name="Left">
                    <Node ID="I16" type="I2"/>
                </Graph>
                <Graph ID="I17" kind="RHS" name="Right">
                    <Node ID="I18" type="I2"/>
                </Graph>
                <Morphism name="rule2"/>
                <TaggedValue Tag="layer" TagValue="0"/>
                <TaggedValue Tag="priority" TagValue="0"/>
            </Rule>
            <RuleSequences>
                <Sequence name="TestSequence" trafoByARS="true">
                    <Graph id="I5"/>
                    <Subsequence iterations="1">
                        <Item iterations="1" rule="rule1"/>
                        <Item iterations="1" rule="rule2"/>
                    </Subsequence>
                </Sequence>
            </RuleSequences>
        </GraphTransformationSystem>
        <RuleSequences/>
    </RuleSequenceApplicability>
</Document>
