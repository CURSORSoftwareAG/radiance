/*
 * Copyright (c) 2005-2019 Radiance Kirill Grouchnikov. All Rights Reserved.
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
 *  o Neither the name of the copyright holder nor the names of
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
package org.pushingpixels.substance.api.renderer;

import org.pushingpixels.substance.api.*;
import org.pushingpixels.substance.api.SubstanceSlices.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.colorscheme.SubstanceColorScheme;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker.StateContributionInfo;
import org.pushingpixels.substance.internal.ui.SubstanceListUI;
import org.pushingpixels.substance.internal.utils.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.*;
import java.awt.*;
import java.util.Map;

/**
 * Default renderer for list cells. Use {@link SubstancePanelListCellRenderer} for more
 * complex renderers that need more than one label for their content.
 *
 * @author Kirill Grouchnikov
 */
@SubstanceRenderer
public class SubstanceDefaultListCellRenderer extends DefaultListCellRenderer
        implements ThemedIconAwareRenderer {
    protected float rolloverArmAmount;

    /**
     * Constructs a default renderer object for an item in a list.
     */
    public SubstanceDefaultListCellRenderer() {
        super();
        SubstanceCortex.ComponentOrParentChainScope.setColorizationFactor(this, 1.0);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        this.setComponentOrientation(list.getComponentOrientation());
        this.rolloverArmAmount = 0.0f;

        ListUI listUI = list.getUI();
        if (listUI instanceof SubstanceListUI) {
            SubstanceListUI ui = (SubstanceListUI) listUI;

            StateTransitionTracker.ModelStateInfo modelStateInfo = ui.getModelStateInfo(index,
                    this);
            ComponentState currState = ui.getCellState(index, this);

            // special case for drop location
            JList.DropLocation dropLocation = list.getDropLocation();
            boolean isDropLocation = (dropLocation != null && !dropLocation.isInsert()
                    && dropLocation.getIndex() == index);

            if (!isDropLocation && (modelStateInfo != null)) {
                Map<ComponentState, StateContributionInfo> activeStates =
                        modelStateInfo.getStateContributionMap();
                SubstanceColorScheme colorScheme = getColorSchemeForState(list, ui, currState);
                if (currState.isDisabled() || (activeStates == null)
                        || (activeStates.size() == 1)) {
                    super.setForeground(new ColorUIResource(colorScheme.getForegroundColor()));
                    this.rolloverArmAmount = 0.0f;
                } else {
                    float aggrRed = 0;
                    float aggrGreen = 0;
                    float aggrBlue = 0;

                    for (Map.Entry<ComponentState, StateTransitionTracker.StateContributionInfo> activeEntry :
                            modelStateInfo.getStateContributionMap().entrySet()) {
                        ComponentState activeState = activeEntry.getKey();
                        float contribution = activeEntry.getValue().getContribution();
                        if (activeState.isFacetActive(SubstanceSlices.ComponentStateFacet.ROLLOVER)
                                || activeState.isFacetActive(
                                SubstanceSlices.ComponentStateFacet.ARM)) {
                            this.rolloverArmAmount = Math.max(this.rolloverArmAmount, contribution);
                        }

                        SubstanceColorScheme scheme = getColorSchemeForState(list, ui, activeState);
                        Color schemeFg = scheme.getForegroundColor();
                        aggrRed += schemeFg.getRed() * contribution;
                        aggrGreen += schemeFg.getGreen() * contribution;
                        aggrBlue += schemeFg.getBlue() * contribution;
                    }
                    super.setForeground(new ColorUIResource(
                            new Color((int) aggrRed, (int) aggrGreen, (int) aggrBlue)));
                }
            } else {
                SubstanceColorScheme scheme = getColorSchemeForState(list, ui, currState);
                if (isDropLocation) {
                    scheme = SubstanceColorSchemeUtilities.getColorScheme(list,
                            ColorSchemeAssociationKind.HIGHLIGHT_TEXT, currState);
                }
                boolean isActive = currState.isFacetActive(
                        SubstanceSlices.ComponentStateFacet.ROLLOVER) ||
                        currState.isFacetActive(SubstanceSlices.ComponentStateFacet.SELECTION) ||
                        currState.isFacetActive(SubstanceSlices.ComponentStateFacet.ARM);
                this.rolloverArmAmount = isActive ? 1.0f : 0.0f;
                super.setForeground(new ColorUIResource(scheme.getForegroundColor()));
            }
        } else {
            if (isSelected) {
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setForeground(list.getForeground());
            }
        }

        if (SubstanceCoreUtilities.isCurrentLookAndFeel()
                && (list.getLayoutOrientation() == JList.VERTICAL)) {
            SubstanceStripingUtils.applyStripedBackground(list, index, this);
        }

        if (value instanceof Icon) {
            this.setIcon((Icon) value);
            this.setText("");
        } else {
            this.setIcon(null);
            this.setText((value == null) ? "" : value.toString());
        }

        this.setEnabled(list.isEnabled());
        this.setFont(list.getFont());

        Insets ins = SubstanceSizeUtils.getListCellRendererInsets(
                SubstanceSizeUtils.getComponentFontSize(list));
        this.setBorder(new EmptyBorder(ins.top, ins.left, ins.bottom, ins.right));

        this.setOpaque(false);
        return this;
    }

    @Override
    public float getRolloverArmAmount() {
        return this.rolloverArmAmount;
    }

    private SubstanceColorScheme getColorSchemeForState(JList list, SubstanceListUI ui,
            ComponentState state) {
        UpdateOptimizationInfo updateOptimizationInfo = ui.getUpdateOptimizationInfo();
        if (state == ComponentState.ENABLED) {
            if (updateOptimizationInfo == null) {
                return SubstanceColorSchemeUtilities.getColorScheme(list, state);
            } else {
                return updateOptimizationInfo.getDefaultScheme();
            }
        } else {
            if (updateOptimizationInfo == null) {
                return SubstanceColorSchemeUtilities.getColorScheme(list,
                        ColorSchemeAssociationKind.HIGHLIGHT, state);
            } else {
                return updateOptimizationInfo.getHighlightColorScheme(state);
            }
        }
    }

    /**
     * UI resource for renderer (does nothing yet).
     *
     * @author Kirill Grouchnikov
     */
    public static class SubstanceUIResource extends SubstanceDefaultListCellRenderer
            implements javax.swing.plaf.UIResource {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    @Override
    public final void paint(Graphics g) {
        super.paint(g);
    }

    @Override
    protected final void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    @Override
    public final void paintComponents(Graphics g) {
        super.paintComponents(g);
    }

    @Override
    protected final void paintBorder(Graphics g) {
        super.paintBorder(g);
    }
}
