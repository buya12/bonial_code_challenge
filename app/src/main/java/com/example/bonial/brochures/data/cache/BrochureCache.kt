package com.example.bonial.brochures.data.cache

import com.example.bonial.brochures.domain.models.Brochure
import com.example.bonial.core.cache.Cache
import com.example.bonial.core.cache.InMemoryCache
import javax.inject.Inject

class BrochureCache @Inject constructor() : Cache<List<Brochure>> by InMemoryCache()
