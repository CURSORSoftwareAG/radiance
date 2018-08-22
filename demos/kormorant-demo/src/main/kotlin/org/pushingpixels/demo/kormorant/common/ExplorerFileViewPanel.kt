/*
 * Copyright (c) 2005-2018 Kormorant Kirill Grouchnikov.
 * All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of Kormorant, Kirill Grouchnikov nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.pushingpixels.demo.kormorant.common

import org.pushingpixels.flamingo.api.bcb.JBreadcrumbBar
import org.pushingpixels.flamingo.api.common.AbstractFileViewPanel
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState
import org.pushingpixels.flamingo.api.common.JCommandButton
import org.pushingpixels.flamingo.api.common.StringValuePair
import org.pushingpixels.flamingo.api.common.icon.IcoWrapperResizableIcon
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon
import org.pushingpixels.ibis.icon.SvgBatikResizableIcon
import org.pushingpixels.neon.icon.ResizableIcon
import java.awt.Dimension
import java.io.File
import java.io.InputStream
import java.util.*
import javax.swing.filechooser.FileSystemView

/**
 * Panel that hosts image-based buttons.
 *
 * @param <T> Type tag.
 * @author Kirill Grouchnikov
</T> */
class ExplorerFileViewPanel<T>(val bar: JBreadcrumbBar<T>, startingState: CommandButtonDisplayState) :
        AbstractFileViewPanel<T>(startingState) {
    private var useNativeIcons: Boolean = false

    fun setUseNativeIcons(useNativeIcons: Boolean) {
        this.useNativeIcons = useNativeIcons
    }

    override fun toShowFile(pair: StringValuePair<T>): Boolean {
        return true
    }

    override fun getResizableIcon(
            leaf: AbstractFileViewPanel.Leaf,
            stream: InputStream, state: CommandButtonDisplayState, dimension: Dimension): ResizableIcon? {
        var dimension = dimension
        val prefSize = state.preferredIconSize
        if (prefSize > 0) {
            dimension = Dimension(prefSize, prefSize)
        }

        if (this.useNativeIcons) {
            val sourceProp = leaf.getLeafProp("source")
            if (sourceProp is File) {
                val delegate = FileSystemView.getFileSystemView().getSystemIcon(sourceProp)
                if (delegate != null) {
                    return IconWrapperResizableIcon(delegate)
                }
            }
            return null
        }

        val name = leaf.leafName
        val lastPointIndex = name.lastIndexOf('.')
        var ext = "*"
        if (lastPointIndex >= 0) {
            ext = name.substring(lastPointIndex + 1)
        }
        ext = ext.toLowerCase()
        if (ext.compareTo("jpg") == 0 || ext.compareTo("jpeg") == 0
                || ext.compareTo("gif") == 0 || ext.compareTo("png") == 0
                || ext.compareTo("bmp") == 0) {
            return ImageWrapperResizableIcon.getIcon(stream, dimension)
        }

        if (ext.compareTo("svg") == 0) {
            return SvgBatikResizableIcon.getSvgIcon(stream, dimension)
        }

        if (ext.compareTo("svgz") == 0) {
            return SvgBatikResizableIcon.getSvgzIcon(stream, dimension)
        }

        if (ext.compareTo("ico") == 0) {
            return IcoWrapperResizableIcon.getIcon(stream, dimension)
        }

        var icon: ResizableIcon? = iconMapping[ext]
        if (icon == null) {
            try {
                val className = "org.pushingpixels.demo.flamingo.svg.filetypes.transcoded.ext_$ext"
                val transcodedClass = Class.forName(className)
                if (transcodedClass != null) {
                    val of = transcodedClass.getDeclaredMethod("of", Int::class.javaPrimitiveType,
                            Int::class.javaPrimitiveType)
                    icon = of.invoke(null, prefSize, prefSize) as ResizableIcon
                    iconMapping[ext] = icon
                }
            } catch (t: Throwable) {
            }

        }
        return icon
    }

    override fun configureCommandButton(leaf: AbstractFileViewPanel.Leaf, button: JCommandButton,
            icon: ResizableIcon) {
        val filename = leaf.leafName
        var ext = "Generic"
        val lastDot = filename.lastIndexOf('.')
        if (lastDot >= 0)
            ext = filename.substring(lastDot + 1).toUpperCase()
        button.extraText = "$ext file"
    }

    override fun getLeafContent(leaf: T): InputStream? {
        return bar.callback.getLeafContent(leaf)
    }

    companion object {
        private val iconMapping = HashMap<String, ResizableIcon>()
    }
}
