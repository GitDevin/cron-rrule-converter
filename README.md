# CRON RRule Converter
`cron-rrule-converter` is a small utility that converts CRON string to RRule [RFC 5545](https://www.rfc-editor.org/rfc/rfc5545) string. This is NOT a complete solution. Special character `W` alone in day of month field is not supported. `WL` is supported.
## Technology Stack
`cron-rrule-converter` is implemented in `Java` using `cron-utils` library.
