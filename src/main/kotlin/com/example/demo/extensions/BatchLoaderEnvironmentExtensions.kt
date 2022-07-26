package com.example.demo.extensions

import org.dataloader.BatchLoaderEnvironment

inline fun <reified T> BatchLoaderEnvironment.getLoadContext(): T? =
    this.keyContextsList.filterIsInstance<T>().firstOrNull()
