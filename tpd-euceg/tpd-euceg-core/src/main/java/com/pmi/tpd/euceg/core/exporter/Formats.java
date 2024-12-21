package com.pmi.tpd.euceg.core.exporter;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.eu.ceg.AttachmentRef;
import org.eu.ceg.CountryValue;
import org.eu.ceg.Date;
import org.eu.ceg.EcigProduct;
import org.eu.ceg.EcigProductSubmission;
import org.eu.ceg.EcigProductTypeEnum;
import org.eu.ceg.NationalMarketValue;
import org.eu.ceg.SubmissionTypeEnum;
import org.eu.ceg.TobaccoProduct;
import org.eu.ceg.TobaccoProductSubmission;
import org.joda.time.DateTime;

import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.binding.BooleanNullable;

public class Formats {

    public static boolean has(final List<?> value) {
        return value != null && !FluentIterable.from(value).filter(Predicates.notNull()).isEmpty();
    }

    public static String trim(final String value) {
        return value != null ? value.toString().replaceAll("\n", "").trim() : value;
    }

    public static boolean exists(final Object value) {
        return value != null;
    }

    public static int size(final List<?> value) {
        if (value != null) {
            return value.size();
        }
        return 0;
    }

    public static String joinAttachmentRef(final BaseExcelExporter<?> context, final List<AttachmentRef> values) {
        if (values != null) {
            return values.stream()
                    .filter(Predicates.notNull())
                    .map(AttachmentRef::getAttachmentID)
                    .map(id -> context.getDataProvider().getAttachementFilename(id))
                    .collect(Collectors.joining(";"));
        }
        return null;
    }

    public static String joinAttattachment(final BaseExcelExporter<?> context, final List<String> values) {
        if (values != null) {
            return values.stream()
                    .filter(Predicates.notNull())
                    .map(id -> context.getDataProvider().getAttachementFilename(id))
                    .collect(Collectors.joining(";"));
        }
        return null;
    }

    public static Object joinCountry(final List<NationalMarketValue> values) {
        if (values != null) {
            return joinCountryCode(values.stream()
                    .filter(Predicates.notNull())
                    .map(NationalMarketValue::value)
                    .collect(Collectors.toList()));
        }
        return null;
    }

    public static Object joinCountryCode(final List<String> values) {
        if (values != null) {
            return values.stream().map(Formats::countryName).map(String::valueOf).collect(Collectors.joining(", "));
        }
        return null;
    }

    public static Object join(final List<Object> values) {
        if (values != null) {
            return values.stream().map(String::valueOf).collect(Collectors.joining(";"));
        }
        return null;
    }

    public static String att(final BaseExcelExporter<?> context, final String uid) {
        if (Strings.isNullOrEmpty(uid)) {
            return null;
        }
        return context.getDataProvider().getAttachementFilename(uid);
    }

    public static Object fromEnum(final List<?> value) throws Exception {
        if (!value.isEmpty()) {
            final Object val = Iterables.getFirst(value, null);
            final Method m = val.getClass().getMethod("value");
            return m.invoke(val);
        }
        return null;
    }

    public static String fromEnumToString(final Object value) throws Exception {
        if (value != null) {
            String val = null;
            if (value instanceof EcigProductTypeEnum) {
                final com.pmi.tpd.euceg.core.refs.EcigProductTypeEnum productType = com.pmi.tpd.euceg.core.refs.EcigProductTypeEnum
                        .fromValue((EcigProductTypeEnum) value)
                        .get();
                val = productType.getValue() + " - " + productType.getShortDescription();
            } else if (value instanceof SubmissionTypeEnum) {
                final com.pmi.tpd.euceg.core.refs.SubmissionTypeEnum productType = com.pmi.tpd.euceg.core.refs.SubmissionTypeEnum
                        .fromValue((SubmissionTypeEnum) value)
                        .get();
                val = productType.getValue() + " - " + productType.getShortDescription();
            } else if (value instanceof List) {
                final List<?> l = (List<?>) value;
                if (!l.isEmpty()) {
                    final Object aEnum = Iterables.getFirst(l, null);
                    val = fromEnumToString(aEnum);
                }
            } else {
                final Method m = value.getClass().getMethod("value");
                val = m.invoke(value) + " - " + value.toString();
            }
            return val;
        }
        return null;
    }

    public static String productCategory(final Object value) {
        String val = null;
        if (value instanceof String) {
            final String v = (String) value;
            if (!v.isEmpty()) {
                if ("TobaccoProduct".equals(value) || "TobaccoProductSubmission".equals(value)) {
                    val = "Tobacco Products";
                } else if ("EcigProduct".equals(value) || "EcigProductSubmission".equals(value)) {
                    val = "E-Cigarettes";
                }
            }
        } else if (value instanceof List) {
            final List<?> l = (List<?>) value;
            if (!l.isEmpty()) {
                final Object v = Iterables.getFirst(l, null);

                if ((v instanceof TobaccoProduct) || (v instanceof TobaccoProductSubmission)) {
                    val = "Tobacco Products";
                } else if ((v instanceof EcigProduct) || (v instanceof EcigProductSubmission)) {
                    val = "E-Cigarettes";
                }
            }
        }

        return val;
    }

    public static String bool(final Object value) {
        if (value != null) {
            if (value instanceof List) {
                return (String) ((List) value).stream().findFirst().map(Formats::bool).orElse(null);
            } else if (value instanceof BooleanNullable) {
                final Boolean b = ((BooleanNullable) value).isValue();
                if (b == null) {
                    return null;
                }
                return fromBoolean(b);
            } else if (value instanceof org.eu.ceg.Boolean) {
                return fromBoolean(((org.eu.ceg.Boolean) value).isValue());
            } else if (value instanceof String) {
                return fromBoolean(java.lang.Boolean.valueOf((String) value));
            }
        }
        return null;
    }

    public static String fromBoolean(final boolean value) {
        return value ? "1" : "0";
    }

    public static String yesNo(final boolean value) {
        return value ? "Yes" : "No";
    }

    public static Object fromDate(final Object value) {
        if (value != null) {
            if (value instanceof List) {
                return ((List) value).stream().findFirst().map(Formats::fromDate).orElse(null);
            } else if (value instanceof Date) {
                return Eucegs.printLocalDate(((Date) value).getValue());
            }
        }
        return null;
    }

    public static Object fromDateTime(final DateTime value) {
        if (value != null) {
            return value;
        }
        return null;
    }

    public static Object year(final DateTime value) {
        if (value != null) {
            return value.year().getAsString();
        }
        return null;
    }

    public static Object month(final DateTime value) {
        if (value != null) {
            return value.monthOfYear().getAsText(Locale.ENGLISH);
        }
        return null;
    }

    public static Object decimal(final Object value) {
        if (value != null) {
            if (value instanceof List) {
                return ((List) value).stream().findFirst().map(Formats::decimal).orElse(null);
            } else if (value instanceof org.eu.ceg.Double) {
                return ((org.eu.ceg.Double) value).getValue();
            }
        }
        return null;
    }

    public static Object percentage(final org.eu.ceg.Percentage value) {
        if (value != null) {
            return value.getValue();
        }
        return null;
    }

    public static Object integer(final org.eu.ceg.Integer value) {
        if (value != null) {
            return value.getValue();
        }
        return null;
    }

    public static Object country(final CountryValue value) {
        if (value != null) {
            return value.name();
        }
        return null;
    }

    public static Object countryName(final String countryCode) {
        if (countryCode != null) {
            final Locale locale = new Locale("", countryCode);
            if ("CZ".equals(countryCode)) {
                return "Czech Republic";
            } else {
                return locale.getDisplayCountry(Locale.ENGLISH);
            }
        }
        return null;

    }

    public static Object empty() {
        return "";
    }
}