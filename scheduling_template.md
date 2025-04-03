# Class Scheduling System with Mentor Assignment

## Excel Template Structure

### Sheet 1: Classes

| ClassID | ClassName         | ClassType     | LanguageType | RoomID (Optional) |
| ------- | ----------------- | ------------- | ------------ | ----------------- |
| CLS001  | Java-01           | CODE_ONLY     |              | 101               |
| CLS002  | Japanese-Basic-01 | LANGUAGE_ONLY | JAPANESE     | 102               |
| CLS003  | Korean-Basic-01   | LANGUAGE_ONLY | KOREAN       | 104               |
| CLS004  | Java-Japanese-01  | COMBINED      | JAPANESE     | 103               |
| CLS005  | Java-Korean-01    | COMBINED      | KOREAN       | 105               |

### Sheet 2: Mentors

| MentorID | Name              | Specialization | Type           | MaxHoursPerWeek | MinHoursPerWeek |
| -------- | ----------------- | -------------- | -------------- | --------------- | --------------- |
| 2        | Nguyễn Minh Thắng | CODE           | CodeMentor     | 40              | 25              |
| 3        | Nguyễn Tuấn Khải  | CODE           | CodeMentor     | 38              | 25              |
| 4        | Anh Tuấn          | JAPANESE       | LanguageMentor | 40              | 25              |
| 5        | Anh               | JAPANESE       | LanguageMentor | 35              | 25              |
| 6        | Nguyên            | KOREAN         | LanguageMentor | 40              | 25              |
| 7        | Tuấn              | KOREAN         | LanguageMentor | 38              | 25              |

### Sheet 3: TimeSlots

| SlotID | DayOfWeek | StartTime | EndTime | SlotType   |
| ------ | --------- | --------- | ------- | ---------- |
| 1      | MONDAY    | 07:30     | 09:00   | LANG_SHORT |
| 2      | MONDAY    | 09:00     | 12:00   | LANG_LONG  |
| 3      | MONDAY    | 07:30     | 11:30   | CODE_AM    |
| 4      | MONDAY    | 13:00     | 17:00   | CODE_PM    |
| 5      | TUESDAY   | 07:30     | 09:00   | LANG_SHORT |
| 6      | TUESDAY   | 09:00     | 12:00   | LANG_LONG  |
| 7      | TUESDAY   | 07:30     | 11:30   | CODE_AM    |
| 8      | TUESDAY   | 13:00     | 17:00   | CODE_PM    |
| 9      | WEDNESDAY | 07:30     | 09:00   | LANG_SHORT |
| 10     | WEDNESDAY | 09:00     | 12:00   | LANG_LONG  |
| 11     | WEDNESDAY | 07:30     | 11:30   | CODE_AM    |
| 12     | WEDNESDAY | 13:00     | 17:00   | CODE_PM    |
| 13     | THURSDAY  | 07:30     | 09:00   | LANG_SHORT |
| 14     | THURSDAY  | 09:00     | 12:00   | LANG_LONG  |
| 15     | THURSDAY  | 07:30     | 11:30   | CODE_AM    |
| 16     | THURSDAY  | 13:00     | 17:00   | CODE_PM    |
| 17     | FRIDAY    | 07:30     | 09:00   | LANG_SHORT |
| 18     | FRIDAY    | 09:00     | 12:00   | LANG_LONG  |
| 19     | FRIDAY    | 07:30     | 11:30   | CODE_AM    |
| 20     | FRIDAY    | 13:00     | 17:00   | CODE_PM    |

### Sheet 4: Rooms

| RoomID | RoomName  |
| ------ | --------- |
| 101    | Room A101 |
| 102    | Room A102 |
| 103    | Room B101 |
| 104    | Room B102 |
| 105    | Room C101 |

### Sheet 5: Config

| ConfigKey                | Value |
| ------------------------ | ----- |
| MaxClassesPerRoom        | 5     |
| PriorityForCombinedClass | 3     |
| PriorityForLanguageClass | 2     |
| PriorityForCodeClass     | 1     |

## Scheduling Logic

### Class Types

1. **CODE_ONLY**:

   - Schedule: Flexible, either 7:30-11:30 (morning) or 13:00-17:00 (afternoon)
   - Days: Monday to Friday
   - Mentor: One CodeMentor

2. **LANGUAGE_ONLY**:

   - Schedule: Fixed at 7:30-9:00
   - Days: Monday to Friday
   - Mentor: One LanguageMentor matching the class language type (JAPANESE or KOREAN)

3. **COMBINED**:
   - Option 1:
     - Language: 8:30-11:30 or 9:00-12:00 (morning)
     - Code: 13:00-17:00 (afternoon)
   - Option 2:
     - Code: 7:30-11:30 (morning)
     - Language: 14:00-17:00 (afternoon)
   - Days: Monday to Friday
   - Mentors: Two mentors (one CodeMentor, one LanguageMentor matching the language type)

### Mentor Assignment Rules

1. Each mentor has a specialization (CODE, JAPANESE, KOREAN)
2. Each mentor is assigned a role (CodeMentor or LanguageMentor)
3. Mentors have teaching limits:
   - Maximum: 40 hours per week (default, can be customized)
   - Minimum: 25 hours per week (default, can be customized)
4. The system balances workload by assigning mentors with fewer hours first

## Example Output

After processing the input data, the system will generate schedules with assigned mentors:

| ClassName         | SubjectName | MentorName        | DayOfWeek | StartTime | EndTime | RoomName  |
| ----------------- | ----------- | ----------------- | --------- | --------- | ------- | --------- |
| Java-01           | Code        | Nguyễn Minh Thắng | TUESDAY   | 07:30     | 11:30   | Room A101 |
| Japanese-Basic-01 | Japanese    | Anh Tuấn          | MONDAY    | 07:30     | 09:00   | Room A102 |
| Korean-Basic-01   | Korean      | Nguyên            | MONDAY    | 07:30     | 09:00   | Room B102 |
| Java-Japanese-01  | Japanese    | Anh               | MONDAY    | 09:00     | 12:00   | Room B101 |
| Java-Japanese-01  | Code        | Nguyễn Tuấn Khải  | MONDAY    | 13:00     | 17:00   | Room B101 |
| Java-Korean-01    | Korean      | Tuấn              | WEDNESDAY | 09:00     | 12:00   | Room C101 |
| Java-Korean-01    | Code        | Nguyễn Minh Thắng | WEDNESDAY | 13:00     | 17:00   | Room C101 |

This scheduling ensures:

1. No room is double-booked
2. Mentors don't exceed their maximum teaching hours
3. Different class types follow their specific scheduling rules
4. Mentors are appropriately matched to subjects based on their specialization
