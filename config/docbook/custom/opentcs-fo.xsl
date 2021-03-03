<?xml version='1.0' encoding='UTF-8' ?> 

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/CSL/Format">
  
  <!-- Import the original DocBook FO stylesheet -->
  <xsl:import href="../unpacked/docbook-xsl-1.78.1/fo/docbook.xsl"/>
  
  <!-- Import our custom titlepage stylesheet -->
  <xsl:import href="./opentcs-fo-titlepage.xsl"/>
  
  <!-- Indicate we're using a capable version of FOP -->
  <xsl:param name="fop1.extensions" select="1"></xsl:param>
  
  <!-- Customize the stylesheet -->
  <xsl:param name="paper.type" select="'A4'"/>
  
  <xsl:param name="page.margin.inner" select="'2.0cm'"/>
  <xsl:param name="page.margin.outer" select="'1.5cm'"/>
  <xsl:param name="page.margin.top" select="'1.5cm'"/>
  <xsl:param name="page.margin.bottom" select="'1.5cm'"/>
  
  <xsl:param name="body.start.indent" select="'0pt'"/>
  <xsl:param name="body.font.master" select="11"/>
  
  <xsl:template match="package">
    <xsl:call-template name="inline.monoseq"/>
  </xsl:template>
  
  <xsl:template match="guibutton">
    <xsl:call-template name="inline.italicseq"/>
  </xsl:template>

  <xsl:template match="guiicon">
    <xsl:call-template name="inline.italicseq"/>
  </xsl:template>

  <xsl:template match="guilabel">
    <xsl:call-template name="inline.italicseq"/>
  </xsl:template>

  <xsl:template match="guimenu">
    <xsl:call-template name="inline.italicseq"/>
  </xsl:template>

  <xsl:template match="guimenuitem">
    <xsl:call-template name="inline.italicseq"/>
  </xsl:template>

  <xsl:template match="guisubmenu">
    <xsl:call-template name="inline.italicseq"/>
  </xsl:template>

  <!-- Tips, warnings etc. with graphics. -->
  <!--xsl:param name="admon.graphics" select="1"/-->

  <xsl:param name="xref.with.number.and.title" select="0"/>
  
  <!-- Include chapter numbers in section numbers -->
  <xsl:param name="section.label.includes.component.label" select="1"/>
  
  <xsl:param name="section.autolabel" select="1"/>
  <!-- Configure font sizes for section titles -->
  <xsl:attribute-set name="section.title.level1.properties">
    <xsl:attribute name="font-size">
      <xsl:value-of select="$body.font.master * 1.2"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="section.title.level2.properties">
    <xsl:attribute name="font-size">
      <xsl:value-of select="$body.font.master * 1.2"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="section.title.level3.properties">
    <xsl:attribute name="font-size">
      <xsl:value-of select="$body.font.master * 1.1"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="section.title.level4.properties">
    <xsl:attribute name="font-size">
      <xsl:value-of select="$body.font.master"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="section.title.level5.properties">
    <xsl:attribute name="font-size">
      <xsl:value-of select="$body.font.master"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="section.title.level6.properties">
    <xsl:attribute name="font-size">
      <xsl:value-of select="$body.font.master"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="formal.title.properties" use-attribute-sets="normal.para.spacing">
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="font-size">
      <xsl:value-of select="$body.font.master * 1.2"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
    <xsl:attribute name="text-align">center</xsl:attribute>
    <xsl:attribute name="hyphenate">false</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.4em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.6em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.8em</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:param name="generate.toc">
    /appendix toc,title
    article/appendix  nop
    /article  toc,title
    book      toc,title,table,example,equation
    /chapter  toc,title
    part      toc,title
    /preface  toc,title
    reference toc,title
    /sect1    toc
    /sect2    toc
    /sect3    toc
    /sect4    toc
    /sect5    toc
    /section  toc
    set       toc,title
  </xsl:param>
  <xsl:param name="formal.title.placement">
    figure after
    example after
    equation after
    table after
    procedure after
    task after
  </xsl:param>
</xsl:stylesheet>
