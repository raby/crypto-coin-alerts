package com.alexitc.coinalerts.errors

import com.alexitc.coinalerts.commons.InputValidationError

// Paginated query
sealed trait PaginatedQueryError
case object InvalidQueryOffsetError extends PaginatedQueryError with InputValidationError
case class InvalidQueryLimitError(maxValue: Int) extends PaginatedQueryError with InputValidationError

