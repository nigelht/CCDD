/**
 * CFS Command & Data Dictionary telemetry scheduler dialog. Copyright 2017
 * United States Government as represented by the Administrator of the National
 * Aeronautics and Space Administration. No copyright is claimed in the United
 * States under Title 17, U.S. Code. All Other Rights Reserved.
 */
package CCDD;

import static CCDD.CcddConstants.AUTO_CREATE_ICON;
import static CCDD.CcddConstants.CLOSE_ICON;
import static CCDD.CcddConstants.DELETE_ICON;
import static CCDD.CcddConstants.INSERT_ICON;
import static CCDD.CcddConstants.LABEL_FONT_BOLD;
import static CCDD.CcddConstants.OK_BUTTON;
import static CCDD.CcddConstants.OK_ICON;
import static CCDD.CcddConstants.STORE_ICON;
import static CCDD.CcddConstants.UNDO_ICON;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import CCDD.CcddClasses.DataStream;
import CCDD.CcddClasses.RateInformation;
import CCDD.CcddClasses.ToolTipTreeNode;
import CCDD.CcddClasses.ValidateCellActionListener;
import CCDD.CcddConstants.DialogOption;
import CCDD.CcddConstants.SchedulerType;
import CCDD.CcddConstants.TableTreeType;

/******************************************************************************
 * CFS Command & Data Dictionary telemetry scheduler dialog class
 *****************************************************************************/
@SuppressWarnings("serial")
public class CcddTelemetrySchedulerDialog extends CcddDialogHandler implements CcddSchedulerDialogInterface
{
    // Main class reference
    private final CcddMain ccddMain;
    private final CcddRateParameterHandler rateHandler;
    private final CcddTableTreeHandler allVariableTree;
    private final CcddSchedulerDbIOHandler schedulerDb;
    private final List<CcddSchedulerHandler> schHandlers;
    private CcddSchedulerHandler activeSchHandler;
    private final CcddSchedulerValidator validator;

    // Components references by multiple methods
    private JButton btnAutoFill;
    private JButton btnAssign;
    private JButton btnValidate;
    private JButton btnClear;
    private JButton btnAddSubMessage;
    private JButton btnDeleteSubMessage;
    private JButton btnStore;
    private JButton btnClose;
    private JTabbedPane tabbedPane;

    // List containing the path for all nodes in the variable tree
    private final List<String> allVariableTreePaths;

    /**************************************************************************
     * Telemetry scheduler dialog class constructor
     * 
     * @param ccddMain
     *            main class
     *************************************************************************/
    CcddTelemetrySchedulerDialog(CcddMain ccddMain)
    {
        this.ccddMain = ccddMain;
        rateHandler = ccddMain.getRateParameterHandler();
        schedulerDb = new CcddSchedulerDbIOHandler(ccddMain,
                                                   SchedulerType.TELEMETRY_SCHEDULER,
                                                   this);
        validator = new CcddSchedulerValidator(ccddMain, this);
        schHandlers = new ArrayList<CcddSchedulerHandler>();

        // Create a tree containing all of the variables. This is used for
        // determining bit-packing and variable relative position
        allVariableTree = new CcddTableTreeHandler(ccddMain,
                                                   TableTreeType.INSTANCE_WITH_PRIMITIVES_AND_RATES,
                                                   ccddMain.getMainFrame());

        // Expand the tree so that all nodes are 'visible'
        allVariableTree.setTreeExpansion(true);

        allVariableTreePaths = new ArrayList<String>();

        // Step through all of the nodes in the variable tree
        for (Enumeration<?> element = allVariableTree.getRootNode().preorderEnumeration(); element.hasMoreElements();)
        {
            // Convert the variable path to a string and add it to the list
            allVariableTreePaths.add(allVariableTree.getFullVariablePath(((ToolTipTreeNode) element.nextElement()).getPath()));
        }

        // Create he telemetry scheduler dialog
        initialize();
    }

    /**************************************************************************
     * Create the data package editor window
     *************************************************************************/
    private void initialize()
    {
        // Create a button panel
        JPanel buttonPanel = new JPanel();

        // Load the stored telemetry scheduler data from the project database
        schedulerDb.loadStoredData();

        // Auto-fill button
        btnAutoFill = CcddButtonPanelHandler.createButton("Auto-fill",
                                                          AUTO_CREATE_ICON,
                                                          KeyEvent.VK_F,
                                                          "Auto-fill the message table with the variables");

        // Create a listener for the Auto-fill button
        btnAutoFill.addActionListener(new ValidateCellActionListener()
        {
            /******************************************************************
             * Auto-fill the variables into the telemetry scheduler for the
             * currently selected data stream
             *****************************************************************/
            @Override
            protected void performAction(ActionEvent ae)
            {
                // Run auto-fill
                activeSchHandler.autoFill();
            }

            /******************************************************************
             * Get the reference to the currently displayed table
             *****************************************************************/
            @Override
            protected CcddJTableHandler getTable()
            {
                return activeSchHandler.getSchedulerEditor().getTable();
            }
        });

        // Clear Msgs button
        btnClear = CcddButtonPanelHandler.createButton("Clear Msgs",
                                                       UNDO_ICON,
                                                       KeyEvent.VK_R,
                                                       "Remove the variables from all messages");

        // Add a listener for the Clear Msgs button
        btnClear.addActionListener(new ValidateCellActionListener()
        {
            /******************************************************************
             * Remove the variables from all messages in the currently selected
             * data stream
             *****************************************************************/
            @Override
            protected void performAction(ActionEvent ae)
            {
                activeSchHandler.getSchedulerEditor().clearMessages();
            }

            /******************************************************************
             * Get the reference to the currently displayed table
             *****************************************************************/
            @Override
            protected CcddJTableHandler getTable()
            {
                return activeSchHandler.getSchedulerEditor().getTable();
            }
        });

        // Add Sub-msg button
        btnAddSubMessage = CcddButtonPanelHandler.createButton("Add Sub-msg",
                                                               INSERT_ICON,
                                                               KeyEvent.VK_A,
                                                               "Add a sub-message to the currently selected message");

        // Create a listener for the Add Sub-msg button
        btnAddSubMessage.addActionListener(new ValidateCellActionListener()
        {
            /******************************************************************
             * Add a sub-message to the current message
             *****************************************************************/
            @Override
            protected void performAction(ActionEvent ae)
            {
                activeSchHandler.getSchedulerEditor().addSubMessage();
            }

            /******************************************************************
             * Get the reference to the currently displayed table
             *****************************************************************/
            @Override
            protected CcddJTableHandler getTable()
            {
                return activeSchHandler.getSchedulerEditor().getTable();
            }
        });

        // Del Sub-msg button
        btnDeleteSubMessage = CcddButtonPanelHandler.createButton("Del Sub-msg",
                                                                  DELETE_ICON,
                                                                  KeyEvent.VK_D,
                                                                  "Delete the currently selected sub-message");

        // Create a listener for the Del Sub-msg button
        btnDeleteSubMessage.addActionListener(new ValidateCellActionListener()
        {
            /******************************************************************
             * Delete the current sub-message
             *****************************************************************/
            @Override
            protected void performAction(ActionEvent ae)
            {
                activeSchHandler.getSchedulerEditor().deleteSubMessage();
            }

            /******************************************************************
             * Get the reference to the currently displayed table
             *****************************************************************/
            @Override
            protected CcddJTableHandler getTable()
            {
                return activeSchHandler.getSchedulerEditor().getTable();
            }
        });

        // Assign message names and IDs button
        btnAssign = CcddButtonPanelHandler.createButton("Assign Msgs",
                                                        AUTO_CREATE_ICON,
                                                        KeyEvent.VK_M,
                                                        "Automatically assign message names and/or IDs to the messages and sub-messages");

        // Add a listener for the Assign Msgs button
        btnAssign.addActionListener(new ValidateCellActionListener()
        {
            /******************************************************************
             * Automatically assign message names and/or IDs to the messages
             * and sub-messages
             *****************************************************************/
            @Override
            protected void performAction(ActionEvent ae)
            {
                new CcddAssignTelemetryMsgIDDialog(activeSchHandler.getCurrentMessages(),
                                                   CcddTelemetrySchedulerDialog.this);
            }

            /******************************************************************
             * Get the reference to the currently displayed table
             *****************************************************************/
            @Override
            protected CcddJTableHandler getTable()
            {
                return activeSchHandler.getSchedulerEditor().getTable();
            }
        });

        // Validate button
        btnValidate = CcddButtonPanelHandler.createButton("Validate",
                                                          OK_ICON,
                                                          KeyEvent.VK_V,
                                                          "Remove any invalid variables from the messages and sub-messages");

        // Add a listener for the Validate button
        btnValidate.addActionListener(new ValidateCellActionListener()
        {
            /******************************************************************
             * Validate the variables for all of the messages and sub-messages
             *****************************************************************/
            @Override
            protected void performAction(ActionEvent ae)
            {
                // Check if any messages contain invalid entries
                if (activeSchHandler.validateMessages())
                {
                    // Update the assigned variables tree
                    activeSchHandler.getSchedulerEditor().updateAssignmentDefinitions();
                    activeSchHandler.getSchedulerEditor().updateAssignmentList();
                }
            }

            /******************************************************************
             * Get the reference to the currently displayed table
             *****************************************************************/
            @Override
            protected CcddJTableHandler getTable()
            {
                return activeSchHandler.getSchedulerEditor().getTable();
            }
        });

        // Store button
        btnStore = CcddButtonPanelHandler.createButton("Store",
                                                       STORE_ICON,
                                                       KeyEvent.VK_S,
                                                       "Store the message updates in the project database");

        // Add a listener for the Store button
        btnStore.addActionListener(new ValidateCellActionListener()
        {
            /******************************************************************
             * Store the data from the various data streams into the database
             *****************************************************************/
            @Override
            protected void performAction(ActionEvent ae)
            {
                // Check if any message has changed and, if so, that the user
                // confirms storing the changes
                if (isChanges()
                    && new CcddDialogHandler().showMessageDialog(CcddTelemetrySchedulerDialog.this,
                                                                 "<html><b>Store changes?",
                                                                 "Store Changes",
                                                                 JOptionPane.QUESTION_MESSAGE,
                                                                 DialogOption.OK_CANCEL_OPTION) == OK_BUTTON)
                {
                    // Store the messages in the project database
                    storeData();
                }
            }

            /******************************************************************
             * Get the reference to the currently displayed table
             *****************************************************************/
            @Override
            protected CcddJTableHandler getTable()
            {
                return activeSchHandler.getSchedulerEditor().getTable();
            }
        });

        // Create a button to close the dialog
        btnClose = CcddButtonPanelHandler.createButton("Close",
                                                       CLOSE_ICON,
                                                       KeyEvent.VK_C,
                                                       "Close the telemetry scheduler");

        // Add a listener for the Close button
        btnClose.addActionListener(new ValidateCellActionListener()
        {
            /******************************************************************
             * Close the telemetry scheduler dialog
             *****************************************************************/
            @Override
            protected void performAction(ActionEvent ae)
            {
                windowCloseButtonAction();
            }

            /******************************************************************
             * Get the reference to the currently displayed table
             *****************************************************************/
            @Override
            protected CcddJTableHandler getTable()
            {
                return activeSchHandler.getSchedulerEditor().getTable();
            }
        });

        // Add buttons in the order in which they'll appear (left to right, top
        // to bottom)
        buttonPanel.add(btnAutoFill);
        buttonPanel.add(btnAddSubMessage);
        buttonPanel.add(btnAssign);
        buttonPanel.add(btnStore);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnDeleteSubMessage);
        buttonPanel.add(btnValidate);
        buttonPanel.add(btnClose);

        // Create two rows of buttons
        setButtonRows(2);

        // Create a tabbed pane in which to place the scheduler handlers
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(LABEL_FONT_BOLD);

        // Listen for tab selection changes
        tabbedPane.addChangeListener(new ChangeListener()
        {
            /******************************************************************
             * Update the editor to the one associated with the selected tab
             *****************************************************************/
            @Override
            public void stateChanged(ChangeEvent ce)
            {
                // Set the active editor to the one indicated by the currently
                // selected tab
                activeSchHandler = schHandlers.get(tabbedPane.getSelectedIndex());
            }
        });

        // Add the scheduler handlers to the tabbed pane
        addDataStreams();

        // Set the first tab as the active editor
        activeSchHandler = schHandlers.get(0);

        // Display the telemetry scheduler dialog
        showOptionsDialog(ccddMain.getMainFrame(),
                          tabbedPane,
                          buttonPanel,
                          "Telemetry Scheduler",
                          true);
    }

    /**************************************************************************
     * Add a scheduler handler for each rate
     *************************************************************************/
    private void addDataStreams()
    {
        // Get the different rate columns
        String[] rateNames = rateHandler.getRateColumnNames(false);

        // Create a variable to hold the rate information
        RateInformation rateInfo;

        // Step through each rate column
        for (int index = 0; index < rateNames.length; index++)
        {
            // Get the rate parameters for the rate column
            rateInfo = rateHandler.getRateInformationByRateName(rateNames[index]);

            // Create a new scheduler handler for each rate column
            schHandlers.add(new CcddSchedulerHandler(ccddMain, rateNames[index], this));

            // Add each table as a tab in the editor window tabbed pane
            tabbedPane.addTab(rateInfo.getStreamName(),
                              null,
                              schHandlers.get(index).getSchedulerPanel(),
                              null);
        }
    }

    /**************************************************************************
     * Store the data from the various data streams into the database
     *************************************************************************/
    private void storeData()
    {
        // Create a list to hold the data streams
        List<DataStream> streams = new ArrayList<>();

        // Step through all the current scheduler handlers
        for (CcddSchedulerHandler handler : schHandlers)
        {
            // Add a new data stream for each scheduler handler
            streams.add(new DataStream(handler.getCurrentMessages(),
                                       handler.getRateName()));

            // Update the copy of the messages so that subsequent changes can
            // be detected
            handler.getSchedulerEditor().copyMessages();
        }

        // Store the data streams
        schedulerDb.storeData(streams);
    }

    /**************************************************************************
     * Handle the dialog close button press event
     *************************************************************************/
    @Override
    protected void windowCloseButtonAction()
    {
        // Check if the contents of the last cell edited in the scheduler table
        // is validated and that no message has changed. If a change exists
        // then confirm discarding the changes
        if (activeSchHandler.getSchedulerEditor().getTable().isLastCellValid()
            && (!isChanges()
            || new CcddDialogHandler().showMessageDialog(CcddTelemetrySchedulerDialog.this,
                                                         "<html><b>Discard changes?",
                                                         "Discard Changes",
                                                         JOptionPane.QUESTION_MESSAGE,
                                                         DialogOption.OK_CANCEL_OPTION) == OK_BUTTON))
        {
            // Close the telemetry scheduler dialog
            closeDialog();
        }
    }

    /**************************************************************************
     * Check if a change has been made to a message
     * 
     * @return true if a message in any of the data streams changed; false
     *         otherwise
     *************************************************************************/
    private boolean isChanges()
    {
        boolean isChanged = false;

        // Step through each data stream
        for (CcddSchedulerHandler schHandler : schHandlers)
        {
            // Check if a message in the stream changed
            if (schHandler.getSchedulerEditor().isMessagesChanged())
            {
                // Set the flag indicating a change and stop searching
                isChanged = true;
                break;
            }
        }

        return isChanged;
    }

    /**************************************************************************
     * Get the scheduler dialog
     * 
     * @return Scheduler dialog
     *************************************************************************/
    @Override
    public CcddDialogHandler getDialog()
    {
        return CcddTelemetrySchedulerDialog.this;
    }

    /**************************************************************************
     * Enable/disable the dialog controls
     * 
     * @param enable
     *            true to enable the controls, false to disable
     *************************************************************************/
    @Override
    public void setControlsEnabled(boolean enable)
    {
        super.setControlsEnabled(enable);
        activeSchHandler.setArrowsEnabled(enable);
    }

    /**************************************************************************
     * Get the scheduler database handler
     * 
     * @return Scheduler database handler
     *************************************************************************/
    @Override
    public CcddSchedulerDbIOHandler getSchedulerDatabaseHandler()
    {
        return schedulerDb;
    }

    /**************************************************************************
     * Get the scheduler validator
     * 
     * @return Scheduler validator
     *************************************************************************/
    @Override
    public CcddSchedulerValidator getSchedulerValidator()
    {
        return validator;
    }

    /**************************************************************************
     * Create and return a scheduler input object
     * 
     * @param rateName
     *            rate column name
     * 
     * @return Telemetry input object
     *************************************************************************/
    @Override
    public CcddSchedulerInputInterface createSchedulerInput(String rateName)
    {
        return new CcddTelemetrySchedulerInput(ccddMain,
                                               this,
                                               rateName,
                                               allVariableTree,
                                               allVariableTreePaths);
    }

    /**************************************************************************
     * Get the scheduler handler
     * 
     * @return Scheduler handler
     *************************************************************************/
    @Override
    public CcddSchedulerHandler getSchedulerHandler()
    {
        return activeSchHandler;
    }
}