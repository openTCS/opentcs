/*
 * openTCS copyright information:
 * Copyright (c) 2017 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.allocation;

import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import org.opentcs.data.model.TCSResource;

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
  private static final ResourceBundle BUNDLE
      = java.util.ResourceBundle.getBundle("org/opentcs/guing/res/labels");

  /**
   * Creates a new instance.
   */
  public AllocationTreeModel() {
    super(new DefaultMutableTreeNode(BUNDLE.getString("ResourceAllocationPanel.treeRoot.text")),
          true);
  }

  /**
   * Updates the vehicle resource allocations displayed in this tree model.
   *
   * @param newAllocations The new vehicle resource allocations
   */
  public void updateAllocations(Map<String, Set<TCSResource<?>>> newAllocations) {
    newAllocations.forEach(this::updateVehicleAllocation);
    removeNotAllocatedVehicles(newAllocations.keySet());
  }

  /**
   * Removes all vehicle tree nodes where the stored vehicle name does not exist in the new
   * allocation.
   *
   * @param allocatedVehicles The vehicles which have a resource allocation
   */
  private void removeNotAllocatedVehicles(Set<String> allocatedVehicles) {
    for (int x = 0; x < root.getChildCount(); x++) {
      DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) root.getChildAt(x);
      Object userObject = currentNode.getUserObject();
      //If we have a vehicle node but the vehicle name is not in the set, remove it
      if (userObject instanceof String && !allocatedVehicles.contains((String) userObject)) {
        removeNodeFromParent(currentNode);
        x = x - 1;
      }
    }
  }

  /**
   * Updates the allocated resources for a specified vehicle if necessarry.
   *
   * @param vehicleName The name of the vehicle
   * @param resources The allocated resources of the vehicle
   */
  private void updateVehicleAllocation(String vehicleName, Set<TCSResource<?>> resources) {
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

    for (int x = 0; x < vehicleNode.getChildCount(); x++) {
      DefaultMutableTreeNode current = (DefaultMutableTreeNode) vehicleNode.getChildAt(x);
      TCSResource<?> resource = (TCSResource<?>) current.getUserObject();
      if (!resources.contains(resource)) {
        vehicleNode.remove(x);
        x--;
      }
    }
    //Add new resources that are not in the jtree already at the correct position
    int index = 0;
    for (TCSResource<?> resource : resources) {
      if (vehicleNode.getChildCount() <= index) {
        //Insert the resource at this position
        vehicleNode.insert(new DefaultMutableTreeNode(resource, false), index);
      }
      else {
        DefaultMutableTreeNode current = (DefaultMutableTreeNode) vehicleNode.getChildAt(index);
        TCSResource<?> resource2 = (TCSResource<?>) current.getUserObject();
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
  private int getChildIndexOf(TCSResource<?> resource, DefaultMutableTreeNode vehicleNode) {
    @SuppressWarnings("unchecked")
    Enumeration<DefaultMutableTreeNode> enumeration = vehicleNode.children();

    int index = 0;
    while (enumeration.hasMoreElements()) {
      DefaultMutableTreeNode child = enumeration.nextElement();
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
