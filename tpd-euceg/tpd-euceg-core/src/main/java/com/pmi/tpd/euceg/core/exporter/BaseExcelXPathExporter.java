package com.pmi.tpd.euceg.core.exporter;

import static com.google.common.base.Preconditions.checkState;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathFunctionNotFoundException;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.apache.commons.jxpath.PackageFunctions;
import org.apache.commons.jxpath.Pointer;
import org.apache.poi.ss.usermodel.Cell;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pmi.tpd.api.scheduler.ITaskMonitorProgress;
import com.pmi.tpd.euceg.core.excel.ColumnDescriptor;
import com.pmi.tpd.euceg.core.excel.ExcelSheet;
import com.pmi.tpd.euceg.core.excel.ListDescriptor;
import com.pmi.tpd.euceg.core.exporter.submission.ExportOption;

/**
 * @author Christophe Friederich
 * @since 2.5
 */
public abstract class BaseExcelXPathExporter<R> extends BaseExcelExporter<R> {

    /** */
    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final LinkedList<JXPathContext> contextQueue = Lists.newLinkedList();

    private final JXPathContext sharedContext;

    private final Map<String, CompiledExpression> expressions = Maps.newHashMap();

    protected BaseExcelXPathExporter(@Nonnull final ListDescriptor root, @Nonnull final List<ExcelSheet> excelSheets,
            @Nullable final ExportOption options, @Nonnull final IDataProvider<R> dataProvider) {
        super(root, excelSheets, options, dataProvider);
        sharedContext = JXPathContext.newContext(null);
        sharedContext.setLenient(true);
        declareContext();
    }

    @Override
    protected void peek(final R element) {
        initialContext(element);
    }

    protected void declareContext() {
        final JXPathContext context = getSharedContext();

        final FunctionLibrary library = new FunctionLibrary();
        library.addFunctions(new ClassFunctions(Formats.class, "fmt"));
        library.addFunctions(new PackageFunctions("java.util.", "util"));
        context.setFunctions(library);
        context.getVariables().declareVariable("ctxt", this);
        context.getVariables().declareVariable("i", Integer.valueOf(1));
        context.getVariables().declareVariable("j", Integer.valueOf(1));

    }

    protected JXPathContext initialContext(final R bean) {
        sharedContext.setValue("$i", Integer.valueOf(1));
        sharedContext.setValue("$j", Integer.valueOf(1));
        final JXPathContext context = JXPathContext.newContext(sharedContext, bean);
        setCurrentContext(context);
        return context;
    }

    @Override
    protected void export(@Nonnull final OutputStream stream,
        @Nullable final ITaskMonitorProgress monitor,
        @Nonnull final ICallbackExport<R> callback) throws Throwable {
        expressions.clear();
        try {
            super.export(stream, monitor, callback);
        } finally {
            expressions.clear();
        }

    }

    protected JXPathContext getCurrentContext() {
        return contextQueue.peek();
    }

    protected JXPathContext getSharedContext() {
        return sharedContext;
    }

    private void setCurrentContext(final JXPathContext context) {
        contextQueue.clear();
        contextQueue.add(context);
    }

    /**
     * Execute {@code invoker} in relative xpath if exists.
     *
     * @param relativePath
     *                     the relative xpath to use.
     * @param invoker
     *                     to invoke.
     */
    protected void relative(@Nonnull final String relativePath, @Nonnull final Invoker invoker) {
        relative(relativePath, invoker, false);
    }

    /**
     * Execute {@code invoker} in relative xpath if exists.
     *
     * @param relativePath
     *                     the relative xpath to use.
     * @param enforce
     *                     enforce execution of invoker.
     * @param invoker
     *                     to invoke.
     */
    protected void relative(@Nonnull final String relativePath, @Nonnull final Invoker invoker, final boolean enforce) {
        checkState(!Strings.isNullOrEmpty(relativePath), "relativePath can not be null or empty");
        final boolean relativized = push(relativePath);
        if (relativized || enforce) {
            try {
                invoker.invoke();
            } finally {
                if (relativized) {
                    pop();
                }
            }
        }
    }

    private boolean push(@Nonnull final String relativePath) {
        checkState(!Strings.isNullOrEmpty(relativePath), "relativePath can not be null or empty");
        final CompiledExpression expression = getCompiledExpression(relativePath);
        if (expression.getValue(getCurrentContext()) != null) {
            final Pointer pointer = getCurrentContext().getPointer(relativePath);
            contextQueue.push(getCurrentContext().getRelativeContext(pointer));
            return true;
        }
        return false;
    }

    private JXPathContext pop() {
        return contextQueue.pop();
    }

    protected void setVariable(final String variableName, final Object value) {
        getSharedContext().setValue(variableName, value);
    }

    protected <V> V getValue(final String xpath, final Class<V> requiredType) {
        final CompiledExpression expression = getCompiledExpression(xpath);
        return requiredType.cast(expression.getValue(getCurrentContext(), requiredType));
    }

    @Override
    protected void setValue(final String column, final Object value) {
        final Cell cell = getCurrentRow().createCell(getColumnIndex(column));
        if (value != null) {
            cell.setCellValue(value.toString());
        }

    }

    @Override
    protected void setValue(final ColumnDescriptor<?> col) {
        setValueWithXPath(col.getName(),
            col.getMetadata().getXpath(),
            col.getMetadata().getFormat(),
            col.getMetadata().getDefaultValue());
    }

    protected void setValueWithXPath(final String column, final String path) {
        setValueWithXPath(column, path, null, null);
    }

    protected void setValueWithXPath(final String column, String path, final String format, final Object defaultValue) {

        final Cell cell = getCurrentRow().createCell(getColumnIndex(column));
        getColumn(column).ifPresent(c -> {
            if (c.isPrimaryKey()) {
                cell.setCellStyle(columnPrimaryCellStyle);
            } else {
                if (this.getOptions().isStripedRow() && getCounter().intValue() % 2 != 0) {
                    cell.setCellStyle(columnOddCellStyle);
                }
            }
        });

        try {
            JXPathContext context = getCurrentContext();
            if (path.startsWith("root$")) {
                while (context.getParentContext().getContextBean() != null) {
                    context = context.getParentContext();
                }
                path = path.substring("root$".length());
            }
            if (context != null) {
                final CompiledExpression expression = getCompiledExpression(path);
                Object obj = expression.getValue(context);
                if (obj == null) {
                    obj = defaultValue;
                }
                cell.setCellStyle(getPreferredCellStyle(cell));
                if (obj != null) {
                    if (isDate(obj.getClass()) && format != null) {
                        if (obj instanceof DateTime) {
                            cell.setCellValue(((DateTime) obj).toDate());
                        } else {
                            cell.setCellValue((java.util.Date) obj);
                        }
                    } else if (obj instanceof java.lang.Boolean) {
                        cell.setCellValue((String) Formats.fromBoolean((boolean) obj));
                    } else {
                        cell.setCellValue((String) obj.toString());
                    }

                }
            }
        } catch (final JXPathNotFoundException | JXPathFunctionNotFoundException ex) {
            // noop
            LOGGER.info(String.format("Error on column %s: %s", column, ex.getMessage()));
        }
    }

    public CompiledExpression getCompiledExpression(final String xpath) {
        CompiledExpression expression = this.expressions.get(xpath);
        if (expression == null) {
            expression = JXPathContext.compile(xpath);
            this.expressions.put(xpath, expression);
        }
        return expression;
    }

    @FunctionalInterface
    protected interface Invoker {

        void invoke();

    }

}
