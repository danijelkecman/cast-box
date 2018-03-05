package com.cxromos.castbox.injection.component;

import com.cxromos.castbox.injection.PerFragment;
import com.cxromos.castbox.injection.module.FragmentModule;

import dagger.Subcomponent;

/**
 * This component inject dependencies to all Fragments across the application
 */
@PerFragment
@Subcomponent(modules = FragmentModule.class)
public interface FragmentComponent {

}