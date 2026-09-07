package org.shareat.app.data.supabase

// PostgREST carries `in` filters in the query string, and the proxy in front of it caps a request
// line at 8 KB. A uuid costs 37 chars there, so batches stay well inside that budget.
private const val MaxIdsPerRequest = 100

internal suspend fun <ID, T> selectInBatches(
    ids: Collection<ID>,
    select: suspend (List<ID>) -> List<T>,
): List<T> = ids.chunked(MaxIdsPerRequest).flatMap { batch -> select(batch) }
