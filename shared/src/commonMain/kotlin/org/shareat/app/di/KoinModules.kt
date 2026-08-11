package org.shareat.app.di

import org.koin.core.module.Module
import org.koin.dsl.module

val sharedModule: Module = module {}

expect val platformModule: Module
