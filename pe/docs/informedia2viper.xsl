<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		version="1.0"
		xmlns="http://lamp.cfar.umd.edu/viper"
		xmlns:data="http://lamp.cfar.umd.edu/viperdata"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

	<xsl:template match="/module">
		<viper xsi:schemaLocation="http://lamp.cfar.umd.edu/viper http://documents.cfar.umd.edu/LAMP/Media/Projects/ViPER/Documents/viper.xsd
                                    http://lamp.cfar.umd.edu/viperdata http://documents.cfar.umd.edu/LAMP/Media/Projects/ViPER/Documents/viperdata.xsd">
			<xsl:text>&#xA;</xsl:text>
			<config>
				<xsl:text>&#xA;	</xsl:text>
				<descriptor type="FILE" name="source">
					<xsl:text>&#xA;		</xsl:text>
					<attribute name="media_type" type="svalue"/>
					<xsl:text>&#xA;		</xsl:text>
					<attribute name="media_format" type="svalue"/>
					<xsl:text>&#xA;		</xsl:text>
					<attribute name="media_locator" type="svalue"/>
					<xsl:text>&#xA;		</xsl:text>
					<attribute name="time_range" type="svalue"/>
					<xsl:text>&#xA;		</xsl:text>
					<attribute name="frame_rate" type="svalue"/>
					<xsl:text>&#xA;	</xsl:text>
				</descriptor>
				<xsl:for-each select="//object">
					<xsl:call-template name="descriptor-config-objects"/>
				</xsl:for-each>
				<xsl:text>&#xA;</xsl:text>
			</config>
			<xsl:text>&#xA;</xsl:text>
			<data>
				<xsl:for-each select="//input">
					<xsl:call-template name="sourcefiles"/>
				</xsl:for-each>
			</data>
			<xsl:text>&#xA;</xsl:text>
		</viper>
	</xsl:template>

	<xsl:template name="descriptor-config-objects">
		<xsl:variable name="type" select="@object_type"/>
		<xsl:if test="not(boolean(preceding-sibling::object[@object_type = $type]))">
			<xsl:text>&#xA;	</xsl:text>
			<descriptor type="OBJECT">
				<xsl:attribute name="name">
					<xsl:value-of select="$type"/>
				</xsl:attribute>

				<xsl:if test="../object[@object_type=$type]/@indexed_text">
					<xsl:text>&#xA;		</xsl:text>
					<attribute name="indexed_text" type="svalue" dynamic="false"/>
				</xsl:if>
				<xsl:if test="../object[@object_type=$type]/@label_text">
					<xsl:text>&#xA;		</xsl:text>
					<attribute name="label_text" type="svalue" dynamic="false"/>
				</xsl:if>
				<xsl:if test="../object[@object_type=$type]/@detection_confidence">
					<xsl:text>&#xA;		</xsl:text>
					<attribute name="detection_confidence" type="dvalue" dynamic="false"/>
				</xsl:if>
				<xsl:if test="../object[@object_type=$type]/@recognition_confidence">
					<xsl:text>&#xA;		</xsl:text>
					<attribute name="recognition_confidence" type="dvalue" dynamic="false"/>
				</xsl:if>
				<xsl:if test="../object[@object_type=$type]/@bounding_box">
					<xsl:text>&#xA;		</xsl:text>
					<attribute name="bounding_box" type="bbox" dynamic="false"/>
				</xsl:if>
				<xsl:if test="../object[@object_type=$type]/@centroid">
					<xsl:text>&#xA;		</xsl:text>
					<attribute name="centroid" type="point" dynamic="false"/>
				</xsl:if>
				<xsl:if test="../object[@object_type=$type]/@moving_direction">
					<xsl:text>&#xA;		</xsl:text>
					<attribute name="moving_direction" type="dvalue" dynamic="false"/>
				</xsl:if>
				<xsl:if test="../object[@object_type=$type]/motion_tracking/@bounding_box">
					<xsl:text>&#xA;		</xsl:text>
					<attribute name="tracking_bounding_box" type="bbox" dynamic="true"/>
				</xsl:if>
				<xsl:if test="../object[@object_type=$type]/motion_tracking/@centroid">
					<xsl:text>&#xA;		</xsl:text>
					<attribute name="tracking_centroid" type="point" dynamic="true"/>
				</xsl:if>

				<xsl:for-each select="../object[@object_type=$type]/characteristic">
					<xsl:variable name="char_name" select="string(@name)"/>
					<xsl:if test="not(boolean(../preceding-sibling::object[@object_type = $type]/characteristic[@name = $char_name]))">
						<xsl:text>&#xA;		</xsl:text>
						<attribute name="{$char_name}" type="svalue" dynamic="false"/>
					</xsl:if>
				</xsl:for-each>

				<xsl:text>&#xA;	</xsl:text>
			</descriptor>
		</xsl:if>
	</xsl:template>



	<xsl:template name="attribute-svalue">
		<xsl:param name="attr-name"/>
		<xsl:param name="inform-attr-name" select="$attr-name"/>
		<xsl:param name="viper-attr-name" select="$attr-name"/>
		<xsl:text>&#xA;			</xsl:text>
		<attribute>
			<xsl:attribute name="name">
				<xsl:value-of select="$viper-attr-name"/>
			</xsl:attribute>
			<xsl:text>&#xA;				</xsl:text>
			<xsl:choose>
				<xsl:when test="attribute::*[local-name()=$inform-attr-name]">
					<data:svalue>
						<xsl:attribute name="value">
							<xsl:value-of select="attribute::*[local-name()=$inform-attr-name]"/>
						</xsl:attribute>
					</data:svalue>
				</xsl:when>
				<xsl:otherwise>
					<data:null/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:text>&#xA;			</xsl:text>
		</attribute>
	</xsl:template>

	<xsl:template name="attribute-dvalue">
		<xsl:param name="attr-name"/>
		<xsl:param name="inform-attr-name" select="$attr-name"/>
		<xsl:param name="viper-attr-name" select="$attr-name"/>
		<xsl:text>&#xA;			</xsl:text>
		<attribute>
			<xsl:attribute name="name">
				<xsl:value-of select="$viper-attr-name"/>
			</xsl:attribute>
		<xsl:text>&#xA;				</xsl:text>
			<xsl:choose>
				<xsl:when test="attribute::*[local-name()=$inform-attr-name]">
					<data:dvalue>
						<xsl:attribute name="value">
							<xsl:value-of select="attribute::*[local-name()=$inform-attr-name]"/>
						</xsl:attribute>
					</data:dvalue>
				</xsl:when>
				<xsl:otherwise>
					<data:null/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:text>&#xA;			</xsl:text>
		</attribute>
	</xsl:template>

	<xsl:template name="attribute-point">
		<xsl:param name="attr-name"/>
		<xsl:param name="inform-attr-name" select="$attr-name"/>
		<xsl:param name="viper-attr-name" select="$attr-name"/>
		<xsl:text>&#xA;			</xsl:text>
		<attribute>
			<xsl:attribute name="name">
				<xsl:value-of select="$viper-attr-name"/>
			</xsl:attribute>
			<xsl:text>&#xA;				</xsl:text>
			<xsl:choose>
				<xsl:when test="attribute::*[local-name()=$inform-attr-name]">
					<xsl:variable name="point" select="string(attribute::*[local-name()=$inform-attr-name])"/>
					<data:point>
						<xsl:attribute name="x">
							<xsl:value-of select="number(substring-before($point, ' '))"/>
						</xsl:attribute>
						<xsl:attribute name="y">
							<xsl:value-of select="number(substring-after($point, ' '))"/>
						</xsl:attribute>
					</data:point>
				</xsl:when>
				<xsl:otherwise>
					<data:null/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:text>&#xA;			</xsl:text>
		</attribute>
	</xsl:template>

	<xsl:template name="attribute-bbox">
		<xsl:param name="attr-name"/>
		<xsl:param name="inform-attr-name" select="$attr-name"/>
		<xsl:param name="viper-attr-name" select="$attr-name"/>
		<xsl:text>&#xA;			</xsl:text>
		<attribute>
			<xsl:attribute name="name">
				<xsl:value-of select="$viper-attr-name"/>
			</xsl:attribute>
			<xsl:text>&#xA;				</xsl:text>
			<xsl:choose>
				<xsl:when test="attribute::*[local-name()=$inform-attr-name]">
					<xsl:variable name="box" select="string(attribute::*[local-name()=$inform-attr-name])"/>
					<xsl:variable name="ulx" select="number(substring-before($box, ' '))"/>
					<xsl:variable name="uly" select="number(substring-before(substring-after($box, ' '), ' '))"/>
					<xsl:variable name="rest" select="substring-after(substring-after($box, ' '), ' ')"/>
					<xsl:variable name="lrx" select="number(substring-before($rest, ' '))"/>
					<xsl:variable name="lry" select="number(substring-after($rest, ' '))"/>
					<data:bbox x="{$ulx}" y="{$uly}">
						<xsl:attribute name="width">
							<xsl:value-of select="$lrx - $ulx"/>
						</xsl:attribute>
						<xsl:attribute name="height">
							<xsl:value-of select="$lry - $uly"/>
						</xsl:attribute>
					</data:bbox>
				</xsl:when>
				<xsl:otherwise>
					<data:null/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:text>&#xA;			</xsl:text>
		</attribute>
	</xsl:template>

	<xsl:template name="sourcefiles">
		<xsl:text>&#xA;	</xsl:text>
		<sourcefile>
			<xsl:attribute name="filename">
				<xsl:value-of select="@media_name"/>
			</xsl:attribute>
		<xsl:text>&#xA;		</xsl:text>
			<file name="source">
				<xsl:attribute name="id">
					<xsl:value-of select="@input_id"/>
				</xsl:attribute>
				<xsl:attribute name="framespan">
					<xsl:value-of select="@frame_range"/>
				</xsl:attribute>

				<xsl:call-template name="attribute-svalue">
					<xsl:with-param name="attr-name" select="'media_type'"/>
				</xsl:call-template>
				<xsl:call-template name="attribute-svalue">
					<xsl:with-param name="attr-name" select="'media_format'"/>
				</xsl:call-template>
				<xsl:call-template name="attribute-svalue">
					<xsl:with-param name="attr-name" select="'media_locator'"/>
				</xsl:call-template>
				<xsl:call-template name="attribute-svalue">
					<xsl:with-param name="attr-name" select="'time_range'"/>
				</xsl:call-template>
				<xsl:call-template name="attribute-svalue">
					<xsl:with-param name="attr-name" select="'frame_rate'"/>
				</xsl:call-template>
				<xsl:text>&#xA;		</xsl:text>
			</file>

			<xsl:variable name="file-span-start" select="number(substring-before(string(@frame_range), ':'))"/>
			<xsl:variable name="file-span-end" select="number(substring-after(string(@frame_range), ':'))"/>
			<xsl:variable name="file-time-start" select="number(substring-before(string(@time_range), ':'))"/>
			<xsl:variable name="file-time-end" select="number(substring-after(string(@time_range), ':'))"/>

			<xsl:variable name="rate" select="number(/module/module_input/input/@frame_rate)"/>
			<xsl:variable name="file-span-start-conv" select="round($rate * $file-time-start)"/>
			<xsl:variable name="file-span-end-conv" select="round($rate * $file-time-end)"/>
			<xsl:variable name="file-time-start-conv" select="round($file-span-start div $rate)"/>
			<xsl:variable name="file-time-end-conv" select="round($file-span-end div $rate)"/>


			<xsl:for-each select="//object[@input_reference = current()/@input_id]">
			<!-- if the first frame is before or at the last frame 
			     and the last frame is after or at the first frame -->
			<!-- this assumes time v. frame will either both be used
			     or that one will be used consistently throughout a given
			     Informedia file. -->
				<xsl:variable name="obj-span-start" select="number(substring-before(string(@frame_range), ':'))"/>
				<xsl:variable name="obj-span-end" select="number(substring-after(string(@frame_range), ':'))"/>

				<xsl:variable name="obj-time-start" select="number(substring-before(string(@time_range), ':'))"/>
				<xsl:variable name="obj-time-end" select="number(substring-after(string(@time_range), ':'))"/>

				<xsl:choose>
					<xsl:when test="(($file-span-start &lt;= $obj-span-end)
					       and ($file-span-end &gt;= $obj-span-start))
					      or (($file-span-start-conv &lt;= $obj-span-end)
					       and ($file-span-end-conv &gt;= $obj-span-start))

					      or (($file-time-start &lt;= $obj-time-end)
					       and ($file-time-end &gt;= $obj-time-start))
					      or (($file-time-start-conv &lt;= $obj-time-end)
					       and ($file-time-end-conv &gt;= $obj-time-start))">
						<xsl:choose>
							<xsl:when test="@frame_range">
								<xsl:call-template name="descriptor-data"/>
							</xsl:when>
							<xsl:when test="@time_range">
								<xsl:call-template name="descriptor-data">
									<xsl:with-param name="framespan" select="concat(round($rate * number(substring-before(string(@time_range), ':'))),':',round($rate * number(substring-after(string(@time_range), ':'))))"/>
								</xsl:call-template>
							</xsl:when>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="descriptor-data">
							<xsl:with-param name="framespan" select="'1:1'"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
			<xsl:text>&#xA;	</xsl:text>
		</sourcefile>
	</xsl:template>

	<xsl:template name="descriptor-data">
		<xsl:param name="framespan" select="@frame_range"/>
		<xsl:text>&#xA;		</xsl:text>
		<object framespan="{$framespan}">
			<xsl:attribute name="name">
				<xsl:value-of select="@object_type"/>
			</xsl:attribute>
			<xsl:attribute name="id">
				<xsl:value-of select="@object_id"/>
			</xsl:attribute>

			<xsl:variable name="type" select="string(@object_type)"/>

			<xsl:if test="../object[@object_type=$type]/@indexed_text">
				<xsl:text>&#xA;			</xsl:text>
				<xsl:call-template name="attribute-svalue">
					<xsl:with-param name="attr-name" select="'indexed_text'"/>
				</xsl:call-template>
			</xsl:if>
			<xsl:if test="../object[@object_type=$type]/@label_text">
				<xsl:text>&#xA;			</xsl:text>
				<xsl:call-template name="attribute-svalue">
					<xsl:with-param name="attr-name" select="'label_text'"/>
				</xsl:call-template>
			</xsl:if>
			<xsl:if test="../object[@object_type=$type]/@detection_confidence">
				<xsl:text>&#xA;			</xsl:text>
				<xsl:call-template name="attribute-dvalue">
					<xsl:with-param name="attr-name" select="'detection_confidence'"/>
				</xsl:call-template>
			</xsl:if>
			<xsl:if test="../object[@object_type=$type]/@recognition_confidence">
				<xsl:text>&#xA;			</xsl:text>
				<xsl:call-template name="attribute-dvalue">
					<xsl:with-param name="attr-name" select="'recognition_confidence'"/>
				</xsl:call-template>
			</xsl:if>

			<xsl:if test="../object[@object_type=$type]/@bounding_box">
				<xsl:text>&#xA;			</xsl:text>
				<xsl:call-template name="attribute-bbox">
					<xsl:with-param name="attr-name" select="'bounding_box'"/>
				</xsl:call-template>
			</xsl:if>

			<xsl:if test="../object[@object_type=$type]/@centroid">
				<xsl:text>&#xA;			</xsl:text>
				<xsl:call-template name="attribute-point">
					<xsl:with-param name="attr-name" select="'centroid'"/>
				</xsl:call-template>
			</xsl:if>
			<xsl:if test="../object[@object_type=$type]/@moving_direction">
				<xsl:text>&#xA;			</xsl:text>
				<xsl:call-template name="attribute-dvalue">
					<xsl:with-param name="attr-name" select="'moving_direction'"/>
				</xsl:call-template>
			</xsl:if>

			<xsl:if test="../object[@object_type=$type]/motion_tracking/@bounding_box">
				<xsl:call-template name="dynamic-attribute-bbox">
					<xsl:with-param name="attr-name" select="'bounding_box'"/>
					<xsl:with-param name="viper-attr-name" select="'tracking_bounding_box'"/>
				</xsl:call-template>
			</xsl:if>

			<xsl:if test="../object[@object_type=$type]/motion_tracking/@centroid">
				<xsl:call-template name="dynamic-attribute-point">
					<xsl:with-param name="attr-name" select="'centroid'"/>
					<xsl:with-param name="viper-attr-name" select="'tracking_centroid'"/>
				</xsl:call-template>
			</xsl:if>

			<xsl:for-each select="characteristic">
				<xsl:text>&#xA;			</xsl:text>
				<attribute>
					<xsl:attribute name="name">
						<xsl:value-of select="@name"/>
					</xsl:attribute>
					<xsl:text>&#xA;				</xsl:text>
					<data:svalue>
						<xsl:attribute name="value">
							<xsl:value-of select="@value"/>
						</xsl:attribute>
					</data:svalue>
					<xsl:text>&#xA;			</xsl:text>
				</attribute>
			</xsl:for-each>

			<xsl:text>&#xA;		</xsl:text>
		</object>
	</xsl:template>

	<xsl:template name="dynamic-attribute-bbox">
		<xsl:param name="attr-name"/>
		<xsl:param name="inform-attr-name" select="$attr-name"/>
		<xsl:param name="viper-attr-name" select="$attr-name"/>

		<xsl:variable name="rate" select="number(/module/module_input/input/@frame_rate)"/>
		<xsl:text>&#xA;			</xsl:text>
		<attribute name="{$viper-attr-name}">
			<xsl:text>&#xA;				</xsl:text>
			<xsl:for-each select="motion_tracking[@bounding_box]">
				<xsl:choose>
					<xsl:when test="@frame_range">
						<xsl:call-template name="help-dynamic-attribute-bbox"/>
					</xsl:when>
					<xsl:when test="@time_range">
						<xsl:call-template name="help-dynamic-attribute-bbox">
							<xsl:with-param name="framespan" select="concat(round($rate * number(substring-before(string(@time_range), ':'))),':', round($rate * number(substring-after(string(@time_range), ':'))))"/>
						</xsl:call-template>
					</xsl:when>
				</xsl:choose>
			</xsl:for-each>
			<xsl:text>&#xA;			</xsl:text>
		</attribute>
	</xsl:template>
	<xsl:template name="help-dynamic-attribute-bbox">
		<xsl:param name="framespan" select="string(@frame_range)"/>

		<xsl:variable name="box" select="string(@bounding_box)"/>
		<xsl:variable name="ulx" select="number(substring-before($box, ' '))"/>
		<xsl:variable name="uly" select="number(substring-before(substring-after($box, ' '), ' '))"/>
		<xsl:variable name="rest" select="substring-after(substring-after($box, ' '), ' ')"/>
		<xsl:variable name="lrx" select="number(substring-before($rest, ' '))"/>
		<xsl:variable name="lry" select="number(substring-after($rest, ' '))"/>

		<data:bbox span="{$framespan}" x="{$ulx}" y="{$uly}">
			<xsl:attribute name="width">
				<xsl:value-of select="$lrx - $ulx"/>
			</xsl:attribute>
			<xsl:attribute name="height">
				<xsl:value-of select="$lry - $uly"/>
			</xsl:attribute>
		</data:bbox>
	</xsl:template>


	<xsl:template name="dynamic-attribute-point">
		<xsl:param name="attr-name"/>
		<xsl:param name="inform-attr-name" select="$attr-name"/>
		<xsl:param name="viper-attr-name" select="$attr-name"/>

		<xsl:variable name="rate" select="number(/module/module_input/input/@frame_rate)"/>
		<xsl:text>&#xA;			</xsl:text>
		<attribute name="{$viper-attr-name}">
			<xsl:text>&#xA;				</xsl:text>
			<xsl:for-each select="motion_tracking[@centroid]">
				<xsl:choose>
					<xsl:when test="@frame_range">
						<xsl:call-template name="help-dynamic-attribute-point"/>
					</xsl:when>
					<xsl:when test="@time_range">
						<xsl:call-template name="help-dynamic-attribute-point">
							<xsl:with-param name="framespan" select="concat(round($rate * number(substring-before(string(@time_range), ':'))),':',round($rate * number(substring-after(string(@time_range), ':'))))"/>
						</xsl:call-template>
					</xsl:when>
				</xsl:choose>
			</xsl:for-each>
			<xsl:text>&#xA;			</xsl:text>
		</attribute>
	</xsl:template>
	<xsl:template name="help-dynamic-attribute-point">
		<xsl:param name="framespan" select="@frame_range"/>

		<xsl:variable name="x" select="number(substring-before(string(@centroid), ' '))"/>
		<xsl:variable name="y" select="number(substring-after(string(@centroid), ' '))"/>

		<data:point span="{$framespan}" x="{$x}" y="{$y}"/>
	</xsl:template>


</xsl:stylesheet>

