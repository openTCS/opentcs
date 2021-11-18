/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.allocation;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import org.opentcs.data.model.TCSResourceReference;
import static org.opentcs.guing.plugins.panels.allocation.I18nPlantOverviewPanelResourceAllocation.BUNDLE_PATH;

/**
 * A model for a resource allocation tree to display an alphabetically ordered view for vehicle
 * names and their not ordered allocated resources.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class AllocationTreeModel
    extends DefaultTreeModel {

  /**
   * This class' bundle.
   */
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_PATH);

  /**
   * Creates a new instance.
   */
  public AllocationTreeModel() {
    super(new DefaultMutableTreeNode(BUNDLE.getString("resourceAllocationPanel.treeRoot.text")),
          true);
  }

  /**
   * Updates the vehicle resource allocations displayed in this tree model.
   *
   * @param vehicleName The name of the vehicle
   * @param newAllocations The new vehicle resource allocations
   */
  public void updateAllocations(String vehicleName, List<TCSResourceReference<?>> newAllocations) {
    updateVehicleAllocation(vehicleName, newAllocations);
    removeNotAllocatedVehicles(vehicleName, newAllocations);
  }

  /**
   * Removes all vehicle tree nodes where the vehicle does not have any resources allocated.
   *
   * @param allocatedVehicles The vehicles which have a resource allocation
   */
  private void removeNotAllocatedVehicles(String vehicleName,
                                          List<TCSResourceReference<?>> resources) {
    @SuppressWarnings("unchecked")
    List<DefaultMutableTreeNode> rootChildren
        = Collections.list((Enumeration<DefaultMutableTreeNode>) root.children());

    for (DefaultMutableTreeNode currentNode : rootChildren) {
      Object userObject = currentNode.getUserObject();
      //If we have a vehicle node but the vehicle name is not in the set, remove it
      if (userObject.equals(vehicleName) && resources.isEmpty()) {
        removeNodeFromParent(currentNode);
      }
    }
  }

  /**
   * Updates the allocated resources for a specified vehicle if necessarry.
   *
   * @param vehicleName The name of the vehicle
   * @param resources The allocated resources of the vehicle
   */
  private void updateVehicleAllocation(String vehicleName, List<TCSResourceReference<?>> resources) {
    DefaultMutableTreeNode vehicleNode = null;
    for (int x = 0; x < root.getChildCount(); x++) {
      DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) root.getChildAt(x);
      if (currentNode.getUserObject().equals(vehicleName)) {
        vehicleNode = currentNode;
      }
    }
    if (vehicleNode == null) {
      vehicleNode = createNewVehicleNode(vehicleName);
    }
    //Remove all children that are not in the new allocation
    List<DefaultMutableTreeNode> vehicleChildren = Collections.list(vehicleNode.children()).stream()
        .map(treeNode -> (DefaultMutableTreeNode) treeNode)
        .collect(Collectors.toList());
    for (DefaultMutableTreeNode current : vehicleChildren) {
      if (!resources.contains((TCSResourceReference<?>) current.getUserObject())) {
        vehicleNode.remove(current);
      }
    }
    //Add new resources that are not in the jtree already at the correct position
    int index = 0;
    for (TCSResourceReference<?> resource : resources) {
      if (vehicleNode.getChildCount() <= index) {
        //Insert the resource at this position
        vehicleNode.insert(new DefaultMutableTreeNode(resource, false), index);
      }
      else {
        DefaultMutableTreeNode current = (DefaultMutableTreeNode) vehicleNode.getChildAt(index);
        TCSResourceReference<?> resource2 = (TCSResourceReference<?>) current.getUserObject();
        //Check if the resource exists at the current position - then we dont have to do anything
        if (!resource.equals(resource2)) {
          //Check if the resource exists at another position in the children list
          int existIndex = getChildIndexOf(resource, vehicleNode);
          //If the resource already exists at another point, move it to the index
          if (existIndex > 0) {
            vehicleNode.remove(existIndex);
          }
          //Insert the resource at this position
          vehicleNode.insert(new DefaultMutableTreeNode(resource, false), index);
        }
      }
      index++;
    }

    reload(vehicleNode);
  }

  /**
   * Returns the index of the first children containing the resource as user object.
   *
   * @param resource The resource to search for
   * @param vehicleNode The parent node
   * @return The index of the node containing the resource or -1 if not found
   */
  private int getChildIndexOf(TCSResourceReference<?> resource, DefaultMutableTreeNode vehicleNode) {
    int index = 0;

    List<DefaultMutableTreeNode> vehicleChildren = Collections.list(vehicleNode.children()).stream()
        .map(treeNode -> (DefaultMutableTreeNode) treeNode)
        .collect(Collectors.toList());

    for (DefaultMutableTreeNode child : vehicleChildren) {
      if (child.getUserObject().equals(resource)) {
        return index;
      }
      index++;
    }
    return -1;
  }

  /**
   * Creates a new vehicle node and adds it to the root node in alphabetical order.
   *
   * @param vehicleName The name of the vehicle and the user object of the new vehicle node
   * @return The vehicle node
   */
  private DefaultMutableTreeNode createNewVehicleNode(String vehicleName) {
    boolean inserted = false;
    DefaultMutableTreeNode vehicleNode = new DefaultMutableTreeNode(vehicleName);
    for (int x = 0; x < root.getChildCount(); x++) {
      DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) root.getChildAt(x);
      //Insert node alphabetically
      if (!inserted && vehicleName.compareTo((String) currentNode.getUserObject()) <= 0) {
        insertNodeInto(vehicleNode, (MutableTreeNode) root, x);
        inserted = true;
      }
    }
    if (!inserted) {
      insertNodeInto(vehicleNode, (MutableTreeNode) root, root.getChildCount());
    }
    return vehicleNode;
  }
}
