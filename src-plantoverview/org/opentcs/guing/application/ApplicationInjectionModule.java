/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application;

import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.awt.Component;
import java.awt.event.MouseListener;
import javax.inject.Singleton;
import javax.swing.JFrame;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import org.jhotdraw.app.Application;
import org.opentcs.guing.application.action.ActionInjectionModule;
import org.opentcs.guing.application.action.edit.UndoRedoManager;
import org.opentcs.guing.application.menus.MenusInjectionModule;
import org.opentcs.guing.application.toolbar.ToolBarInjectionModule;
import org.opentcs.guing.components.dialogs.DialogsInjectionModule;
import org.opentcs.guing.components.dockable.DockableInjectionModule;
import org.opentcs.guing.components.drawing.DrawingInjectionModule;
import org.opentcs.guing.components.properties.PropertiesInjectionModule;
import org.opentcs.guing.components.tree.AbstractTreeViewPanel;
import org.opentcs.guing.components.tree.BlockMouseListener;
import org.opentcs.guing.components.tree.BlocksTreeViewManager;
import org.opentcs.guing.components.tree.BlocksTreeViewPanel;
import org.opentcs.guing.components.tree.ComponentsTreeViewManager;
import org.opentcs.guing.components.tree.ComponentsTreeViewPanel;
import org.opentcs.guing.components.tree.GroupsMouseAdapter;
import org.opentcs.guing.components.tree.GroupsTreeViewManager;
import org.opentcs.guing.components.tree.GroupsTreeViewPanel;
import org.opentcs.guing.components.tree.TreeMouseAdapter;
import org.opentcs.guing.components.tree.TreeView;
import org.opentcs.guing.components.tree.elements.TreeElementsInjectionModule;
import org.opentcs.guing.exchange.ExchangeInjectionModule;
import org.opentcs.guing.exchange.adapter.ProcessAdapterInjectionModule;
import org.opentcs.guing.model.ModelInjectionModule;
import org.opentcs.guing.transport.TransportInjectionModule;
import org.opentcs.guing.util.MessageDisplay;
import org.opentcs.guing.util.UtilInjectionModule;

/**
 * A Guice module for the openTCS kernel application.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ApplicationInjectionModule
    extends AbstractModule {

  /**
   * The application's event bus.
   */
  private final MBassador<Object> eventBus = new MBassador<>(BusConfiguration.Default());
  /**
   * The application's main frame.
   */
  private final JFrame applicationFrame = new JFrame();

  @Override
  protected void configure() {

    install(new DrawingInjectionModule());
    install(new ExchangeInjectionModule());
    install(new ProcessAdapterInjectionModule());
    install(new UtilInjectionModule());
    install(new ModelInjectionModule());
    install(new PropertiesInjectionModule());
    install(new DialogsInjectionModule());
    install(new TreeElementsInjectionModule());
    install(new ActionInjectionModule());
    install(new MenusInjectionModule());
    install(new ToolBarInjectionModule());
    install(new TransportInjectionModule());
    install(new DockableInjectionModule());

    // Bind global event bus and automatically register every created object.
    bind(new TypeLiteral<MBassador<Object>>() {
    })
        .toInstance(eventBus);
    bindListener(Matchers.any(), new TypeListener() {
      @Override
      public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
        typeEncounter.register(new InjectionListener<I>() {
          @Override
          public void afterInjection(I i) {
            eventBus.subscribe(i);
          }
        });
      }
    });

    bind(ApplicationState.class).in(Singleton.class);

    bind(MessageDisplay.class).to(KernelStatusPanel.class);
    bind(KernelStatusPanel.class).in(Singleton.class);

    bind(UndoRedoManager.class).in(Singleton.class);

    install(new ComponentsTreeViewModule());
    install(new BlocksTreeViewModule());
    install(new GroupsTreeViewModule());

    bind(ProgressIndicator.class)
        .to(SplashFrame.class)
        .in(Singleton.class);
    bind(StatusPanel.class).in(Singleton.class);

    bind(JFrame.class)
        .annotatedWith(ApplicationFrame.class)
        .toInstance(applicationFrame);
    bind(Component.class)
        .annotatedWith(ApplicationFrame.class)
        .toInstance(applicationFrame);

    bind(ViewManager.class)
        .in(Singleton.class);

    bind(Application.class)
        .to(OpenTCSSDIApplication.class)
        .in(Singleton.class);

    bind(OpenTCSView.class).in(Singleton.class);
    bind(GuiManager.class).to(OpenTCSView.class);
  }

  private static class ComponentsTreeViewModule
      extends PrivateModule {

    public ComponentsTreeViewModule() {
    }

    @Override
    protected void configure() {
      // Within this (private) module, there should only be a single tree panel.
      bind(ComponentsTreeViewPanel.class)
          .in(Singleton.class);

      // Bind the tree panel annotated with the given annotation to our single
      // instance and expose only this annotated version.
      bind(AbstractTreeViewPanel.class)
          .to(ComponentsTreeViewPanel.class);
      expose(ComponentsTreeViewPanel.class);

      // Bind TreeView to the single tree panel, too.
      bind(TreeView.class)
          .to(ComponentsTreeViewPanel.class);

      // Bind and expose a single manager for the single tree view/panel.
      bind(ComponentsTreeViewManager.class)
          .in(Singleton.class);
      expose(ComponentsTreeViewManager.class);

      bind(MouseListener.class)
          .to(TreeMouseAdapter.class);
    }
  }

  private static class BlocksTreeViewModule
      extends PrivateModule {

    public BlocksTreeViewModule() {
    }

    @Override
    protected void configure() {
      // Within this (private) module, there should only be a single tree panel.
      bind(BlocksTreeViewPanel.class)
          .in(Singleton.class);

      // Bind the tree panel annotated with the given annotation to our single
      // instance and expose only this annotated version.
      bind(AbstractTreeViewPanel.class)
          .to(BlocksTreeViewPanel.class);
      expose(BlocksTreeViewPanel.class);

      // Bind TreeView to the single tree panel, too.
      bind(TreeView.class)
          .to(BlocksTreeViewPanel.class);

      // Bind and expose a single manager for the single tree view/panel.
      bind(BlocksTreeViewManager.class)
          .in(Singleton.class);
      expose(BlocksTreeViewManager.class);

      bind(MouseListener.class)
          .to(BlockMouseListener.class);
    }
  }

  private static class GroupsTreeViewModule
      extends PrivateModule {

    public GroupsTreeViewModule() {
    }

    @Override
    protected void configure() {
      // Within this (private) module, there should only be a single tree panel.
      bind(GroupsTreeViewPanel.class)
          .in(Singleton.class);

      // Bind the tree panel annotated with the given annotation to our single
      // instance and expose only this annotated version.
      bind(AbstractTreeViewPanel.class)
          .to(GroupsTreeViewPanel.class);
      expose(GroupsTreeViewPanel.class);

      // Bind TreeView to the single tree panel, too.
      bind(TreeView.class)
          .to(GroupsTreeViewPanel.class);

      // Bind and expose a single manager for the single tree view/panel.
      bind(GroupsTreeViewManager.class)
          .in(Singleton.class);
      expose(GroupsTreeViewManager.class);

      bind(MouseListener.class)
          .to(GroupsMouseAdapter.class);
    }
  }
}
